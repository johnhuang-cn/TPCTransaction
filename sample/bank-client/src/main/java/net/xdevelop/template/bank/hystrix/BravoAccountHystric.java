package net.xdevelop.template.bank.hystrix;

import org.springframework.stereotype.Component;

import net.xdevelop.template.bank.service.IBravoAccountService;

@Component
public class BravoAccountHystric implements IBravoAccountService {

	@Override
	public double getAmountByUserId(String userId) {
		throw new RuntimeException("IAlphaAccountService unavailable");
	}

	@Override
	public void setAmountForUserId(String userId, double amount) {
		throw new RuntimeException("IAlphaAccountService unavailable");
	}

	@Override
	public void changeAmountForUserId(String userId, double amount) {
		throw new RuntimeException("IAlphaAccountService unavailable");
	}
	
	@Override
	public void changeAmountForUserId4TPC(String userId, double amount) {
		throw new RuntimeException("IAlphaAccountService unavailable");
	}
}