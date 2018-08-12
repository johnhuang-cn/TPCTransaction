package net.xdevelop.tpc;

import org.springframework.stereotype.Component;

import feign.RequestInterceptor;

/**
 * Transfer the transaction id and timeout through header.
 * 
 * @author John.Huang
 */
@Component
public class FeignInterceptor implements RequestInterceptor{

	@Override
	public void apply(feign.RequestTemplate requestTemplate) {
		Long tid = TPCTransactionManager.getTransactionId();
		if (tid != null) {
			int timeout = (int) ((TPCTransactionManager.getExpired() - System.currentTimeMillis()) / 1000) + 1;
			requestTemplate.header(Constants.TPC_TRANSACTION_ID, tid + "");
			requestTemplate.header(Constants.TPC_TRANSACTION_TIMEOUT, timeout + "");
		}
	}
}