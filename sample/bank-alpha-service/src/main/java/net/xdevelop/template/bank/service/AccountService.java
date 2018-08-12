package net.xdevelop.template.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import net.xdevelop.template.bank.mapper.AccountMapper;
import net.xdevelop.tpc.annotation.TPCTransactional;

@Component
public class AccountService {
	@Autowired
	AccountMapper mapper;
	
	@Autowired
	PlatformTransactionManager tm;
	
	public double getAmountByUserId(String userId) {
		return mapper.getAmountByUserId(userId);
	}
	
	@Transactional
	public void setAmountForUserId(String userId, double amount) {
		mapper.setAmountForUserId(userId, amount);
	}
	
	@Transactional
	public void transferIn(String userId, double amount) {
		int count = mapper.increaseAmount(userId, amount);
		
		// random failed for test
		if (amount == 98 || amount == 99) {
			throw new RuntimeException("Transfer failed: random exception for test.");
		}
		if (count <= 0) {
			throw new RuntimeException("Transfer out failed: no record mathced.");
		}
	}
	
	@Transactional
	public void transferOut(String userId, double amount) {
		int count = mapper.decreaseAmount(userId, amount);
		
		// random failed for test
		if (amount == 97) {
			throw new RuntimeException("Transfer failed: random exception for test.");
		}
		if (count <= 0) {
			throw new RuntimeException("Transfer out failed: no record mathced or insufficient Balance.");
		}
	}
	
	@TPCTransactional
	public void transferIn4TPC(String userId, double amount) throws Exception {
			int count = mapper.increaseAmount(userId, amount);
			
			// random failed for test
			if (amount == 98 || amount == 99) {
				throw new RuntimeException("Transfer failed: random exception for test.");
			}
			if (count <= 0) {
				throw new RuntimeException("Transfer out failed: no record mathced.");
			}
	}
	
	@TPCTransactional
	public void transferOut4TPC(String userId, double amount) {
		int count = mapper.decreaseAmount(userId, amount);
		
		// random failed for test
		if (amount == 97) {
			throw new RuntimeException("Transfer failed: random exception for test.");
		}
		if (count <= 0) {
			throw new RuntimeException("Transfer out failed: no record mathced or insufficient Balance.");
		}
	}
}
