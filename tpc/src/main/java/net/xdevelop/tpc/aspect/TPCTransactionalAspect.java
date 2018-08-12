package net.xdevelop.tpc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.transaction.PlatformTransactionManager;

import net.xdevelop.tpc.TPCExecutorManager;
import net.xdevelop.tpc.TPCTransactionManager;
import net.xdevelop.tpc.TransactionExecutor;
import net.xdevelop.tpc.annotation.TPCTransactional;

/**
 * The aspect for TPCTransactional annotation.
 * It wrapper the origin transaction in an async thread,
 * then hold the transaction and waiting the commit request from sponsor.
 * 
 * @author John.Huang
 */
@Aspect
public class TPCTransactionalAspect {
	@Around("@annotation(net.xdevelop.tpc.annotation.TPCTransactional)&&@annotation(tpcTransactional)")
	public Object execute(ProceedingJoinPoint pjp, TPCTransactional tpcTransactional) throws Throwable {
		String tmName = tpcTransactional.value();
		PlatformTransactionManager tm;
		if (tmName == null || "".equals(tmName)) {
			tm = TPCTransactionManager.getDefaultTransactionManager();
		}
		else {
			tm = (PlatformTransactionManager) TPCTransactionManager.getBean(tmName);
		}
		
		TransactionExecutor executor = new TransactionExecutor(tm) {
			protected void execute() throws Throwable {
				Object resultObj = pjp.proceed();
				this.getResult().put("return", resultObj);
			}
		};
		executor.start();
		TPCExecutorManager.hold(executor);
		
		return executor.getResultByKey("return");
	}
}
