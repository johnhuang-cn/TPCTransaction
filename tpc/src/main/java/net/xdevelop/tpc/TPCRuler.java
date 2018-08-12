package net.xdevelop.tpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

/**
 * Ribbon loadbalance rule. 
 * It choose the same server for the requests in same thread,
 * so that the commit/rollback request would send to the same server that hold the transaction.
 * 
 * @author John.Huang
 */
public class TPCRuler implements IRule {
	private final static Logger logger = LoggerFactory.getLogger(TPCRuler.class); 
	
	private static Class<? extends IRule> RULE_CLASS;
	private IRule rule;
	
	public TPCRuler() {
		init();
	}
	
	private void init() {
		if (RULE_CLASS != null) {
			try {
				rule = RULE_CLASS.newInstance();
			} catch (Exception e) {
				logger.error(String.format("Cannot init ribbon rule for %s: %s", RULE_CLASS.getName(), e.getMessage()));
				rule = new ZoneAvoidanceRule();
			}
		}
		else {
			rule = new ZoneAvoidanceRule();
		}
	}
	
	public final static void setRuleClass(Class<? extends IRule> ruleCls) {
		RULE_CLASS = ruleCls;
	}
	
    @Override
	public void setLoadBalancer(ILoadBalancer lb) {
		this.rule.setLoadBalancer(lb);
	}

	@Override
	public ILoadBalancer getLoadBalancer() {
		return this.rule.getLoadBalancer();
	}

	@Override
    public Server choose(Object key) {
		BaseLoadBalancer lb = (BaseLoadBalancer) getLoadBalancer();
    	String name = lb.getName();
		Server server = TPCTransactionManager.getServerByKey(name);
    	if (server != null) {
    		return server;
    	}
		server = rule.choose(key);
		TPCTransactionManager.cacheServerByKey(name, server);
		return server;
    }
}
