package net.xdevelop.tpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import com.netflix.loadbalancer.Server;

import net.xdevelop.snowflake.SnowflakeUidGenerator;

/**
 * Two phase commit transaction manager on sponsor side.
 * 
 * @author John.Huang
 */
@Component
public class TPCTransactionManager implements ApplicationContextAware {
	private final static Logger logger = LoggerFactory.getLogger(TPCTransactionManager.class);
	private final static int DEFAULT_TIMEOUT = 5;
	
	private static ApplicationContext applicationContext;
	private static ThreadLocal<Long> TRANSACTION_ID = new ThreadLocal<Long>();
	private static ThreadLocal<Long> TRANSACTION_EXPIRED = new ThreadLocal<Long>();
	private static ThreadLocal<HashMap<String, Server>> SERVERS = new ThreadLocal<HashMap<String, Server>>();
	
	@Autowired
	private SnowflakeUidGenerator uidGenerator;
	
	private int timeout = DEFAULT_TIMEOUT;
	
	private RestTemplate restTemplate = new RestTemplateBuilder().build();
	
	public long begin() {
		long uid = uidGenerator.getUID();
		TRANSACTION_ID.set(uid);
		TRANSACTION_EXPIRED.set(System.currentTimeMillis() + timeout * 1000);
		logger.info(String.format("TPC transaction %d begin, timeout: %d", uid, TRANSACTION_EXPIRED.get()));
		return uid;
	}
	
	public TPCTransactionManager timeout(int timeout) {
		this.timeout = timeout;
		return this;
	}
	
	public void commit(@SuppressWarnings("rawtypes") Class...classes ) {
		long expired = TRANSACTION_EXPIRED.get();
		if (System.currentTimeMillis() > expired) {
			rollback(new Error("TPC Transaction timeout"), classes);
			return;
		}
		
		ArrayList<String> list = getUrls(true, classes);
		long tid = TRANSACTION_ID.get();
		
		if (list.size() > 0) {
			Optional<Integer> result = list.parallelStream().map(url -> sendPost(url, tid)).reduce((a, b) -> a + b);
			int total = list.size();
			int failed = total - result.get().intValue();
			if (failed > 0) {
				// TODO: Log transaction commit failed to db
				logger.error(String.format("%d of %d transaction(tid: %d) committed failed", failed, total, tid));
			}
			else {
				logger.info(String.format("TPC transaction %d committed successfully", tid));
			}
		}
		clear();
	}
	
	public void rollback(Throwable e, @SuppressWarnings("rawtypes") Class...classes) {
		ArrayList<String> list = getUrls(false, classes);
		long tid = TRANSACTION_ID.get();
		String errMsg = e != null ? e.getMessage() : "";
		logger.error(String.format("transaction(tid: %d) rollback by error: %s", tid, errMsg));
		
		if (list.size() > 0) {
			Optional<Integer> result = list.parallelStream().map(url -> sendPost(url, tid)).reduce((a, b) -> a + b);
			int total = list.size();
			int failed = total - result.get().intValue();
			if (failed > 0) {
				// TODO: Log transaction rollback failed to db
				logger.error(String.format("Only %d of %d service transaction(tid: %d) rolled back", failed, total, tid));
			}
			else {
				logger.info(String.format("TPC transaction %d rolled back successfully", tid));
			}
		}
		clear();
	}
	
	private void clear() {
		TRANSACTION_ID.remove();
		clearServerCache();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ArrayList<String> getUrls(boolean isCommit, Class...classes) {
		ArrayList<String> list = new ArrayList<String>();
		for (Class cls : classes) {
			FeignClient feignClient = (FeignClient) cls.getAnnotationsByType(FeignClient.class)[0];
			Server server = getServerByKey(feignClient.value());
			if (server == null) {
				continue;
			}
			
			if (isCommit) {
				list.add(String.format("http://%s:%d/tpc/commit/{1}", server.getHost(), server.getPort()));
			}
			else {
				list.add(String.format("http://%s:%d/tpc/rollback/{1}", server.getHost(), server.getPort()));
			}
		}
		return list;
	}
	
	private int sendPost(String url, long tid) {
		try {
			logger.info(String.format("Send post to %s with tid %d", url, tid));
			restTemplate.postForEntity(url, null, Void.class, tid);
			return 1;
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	public static Long getTransactionId() {
		return TRANSACTION_ID.get();
	}
	
	public static long getExpired() {
		return TRANSACTION_EXPIRED.get();
	}
	
	public static Server getServerByKey(String key) {
		HashMap<String, Server> map = SERVERS.get(); 
    	if (map == null) {
    		map = new HashMap<String, Server>();
    		SERVERS.set(map);
    		return null;
    	}
    	else {
    		Server server = map.get(key);
    		return server;
    	}
	}
	
	public static void cacheServerByKey(String key, Server server) {
		HashMap<String, Server> map = SERVERS.get(); 
    	if (map == null) {
    		map = new HashMap<String, Server>();
    		SERVERS.set(map);
    	}
    	logger.debug(String.format("Cached server for key %s", key));
    	map.put(key, server);
	}
	
	public static void clearServerCache() {
		HashMap<String, Server> map = SERVERS.get();
		if (map != null) {
			map.clear();
		}
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		TPCTransactionManager.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static Object getBean(String name) throws BeansException {
		return applicationContext.getBean(name);
	}
	
	public static PlatformTransactionManager getDefaultTransactionManager() throws BeansException {
		return (PlatformTransactionManager) applicationContext.getBean(PlatformTransactionManager.class);
	}
}
