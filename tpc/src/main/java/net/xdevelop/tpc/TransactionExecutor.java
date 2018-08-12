package net.xdevelop.tpc;

import java.util.HashMap;
import java.util.concurrent.locks.LockSupport;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The async transaction executor
 * 
 * @author John.Huang
 */
public abstract class TransactionExecutor extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(TransactionExecutor.class);
	
	private final static String ERROR_KEY = "ERROR";
	public enum Operations {
		COMMIT, ROLLBACK
	}
	
	private Operations operation = Operations.ROLLBACK;
	private PlatformTransactionManager tm;
	private Thread mainThread;
	private HashMap<String, Object> result = new HashMap<String, Object>();
	private boolean success = false;
	private long transactionId = 0;
	private int timeout = 5;
	private long expired = 0;
	
	public TransactionExecutor(PlatformTransactionManager tm) {
		// get tpc-transaction-id from header
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String transactionIdStr = request.getHeader(Constants.TPC_TRANSACTION_ID);
		transactionId = 0;
		try {
			transactionId = Long.parseLong(transactionIdStr);
		}
		catch (Exception e) {
			throw new RuntimeException("Invalid/missing transaction id");
		}
		
		String timeoutStr = request.getHeader(Constants.TPC_TRANSACTION_TIMEOUT);
		try {
			timeout = Integer.parseInt(timeoutStr);
		}
		catch (Exception e) {
			timeout = 5;
		}
		if (timeout <= 0) {
			timeout = 1;
		}
		expired = System.currentTimeMillis() + timeout * 1000;
		
		this.tm = tm;
		this.mainThread = Thread.currentThread();
	}
	
	public void run() {
		logger.info(String.format("TPC Transaction %d begin, timeout: %d", transactionId, timeout));
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
		transactionDefinition.setTimeout(timeout);
		TransactionStatus transactionStatus = tm.getTransaction(transactionDefinition);

		try {
			execute();
			success = true;
			logger.info(String.format("TPC Transaction %d prepared", transactionId));
			LockSupport.unpark(mainThread);
			LockSupport.park(Thread.currentThread());
			
			if (operation == Operations.COMMIT) {
				tm.commit(transactionStatus);
				logger.info(String.format("TPC Transaction %d committed", transactionId));
			}
			else {
				tm.rollback(transactionStatus);
				logger.info(String.format("TPC Transaction %d rolled back", transactionId));
			}
		} catch (Throwable e) {
			try {
				tm.rollback(transactionStatus);
				logger.info(String.format("TPC Transaction %d rollback for error: ", transactionId, e.getMessage()));
			} catch (Exception e1) {
			}
			success = false;
			result.put(ERROR_KEY, e.getMessage());
			LockSupport.unpark(mainThread);
		}
	}
	
	protected void execute() throws Throwable {
		// need be override
	}
	
	public Object getResultByKey(String key) {
		return result.get(key);
	}
	
	public String getErrorMessage() {
		return (String) result.get(ERROR_KEY);
	}

	public Operations getOperation() {
		return operation;
	}

	public void setOperation(Operations operation) {
		this.operation = operation;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public long getTransactionExpired() {
		return expired;
	}
	
	public HashMap<String, Object> getResult() {
		return result;
	}

	public boolean isSuccess() {
		return success;
	}
}
