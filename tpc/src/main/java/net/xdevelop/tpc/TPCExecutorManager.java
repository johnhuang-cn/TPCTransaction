package net.xdevelop.tpc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Transaction executor manager.
 * 
 * @author John.Huang
 */
@Component
public class TPCExecutorManager extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(TPCExecutorManager.class);
	
	private static HashMap<Long, TransactionExecutor> TRAN_THREAD_MAP = new HashMap<Long, TransactionExecutor>();
	private static LinkedList<TransactionExecutor> EXECUTORS = new LinkedList<TransactionExecutor>();
	
	public static void hold(TransactionExecutor executor) {
		LockSupport.park(Thread.currentThread());
		if (executor.isSuccess()) {
			addExecutor(executor);
		}
		else {
			throw new RuntimeException(executor.getErrorMessage());
		}
	}
	
	public static void commit(long transactionId) {
		TransactionExecutor t = removeExecutor(transactionId);
		if (t != null) {
			logger.info(String.format("Transaction %d start commit...", transactionId));
			t.setOperation(TransactionExecutor.Operations.COMMIT);
			LockSupport.unpark(t);
		}
	}
	
	public static void rollback(long transactionId) {
		TransactionExecutor t = removeExecutor(transactionId);
		if (t != null) {
			logger.info(String.format("Transaction %d start roll back...", transactionId));
			t.setOperation(TransactionExecutor.Operations.ROLLBACK);
			LockSupport.unpark(t);
		}
	}
	
	private static TransactionExecutor removeExecutor(long transactionId) {
		TransactionExecutor executor;
		synchronized(TRAN_THREAD_MAP) {
			executor = TRAN_THREAD_MAP.remove(transactionId);
		}
		if (executor != null) {
			synchronized(EXECUTORS) {
				EXECUTORS.remove(executor);
			}
		}
		return executor;
	}
	
	private static void addExecutor(TransactionExecutor executor) {
		synchronized(TRAN_THREAD_MAP) {
			TRAN_THREAD_MAP.put(executor.getTransactionId(), executor);
		}
		synchronized(EXECUTORS) {
			EXECUTORS.add(executor);
		}
	}
	
	public void run() {
		logger.info("TPCExecutor timeout monitor starting...");
		TransactionExecutor[] ts = new TransactionExecutor[0];
		while(true) {
			try {
				synchronized (EXECUTORS) {
					ts = EXECUTORS.toArray(ts);
				}
				long now = System.currentTimeMillis();
				for (TransactionExecutor t : ts) {
					if (now > t.getTransactionExpired()) {
						logger.info(String.format("TPC Transaction %d timeout", t.getTransactionId()));
						rollback(t.getTransactionId());
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} catch (Exception e) {
			}
		}
	}
}
