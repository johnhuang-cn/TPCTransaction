package net.xdevelop.template.bank.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.xdevelop.template.bank.hystrix.AlphaAccountHystric;

@FeignClient(value = "bank-alpha", fallback = AlphaAccountHystric.class)
public interface IAlphaAccountService {
	@GetMapping(value = "/accounts/{userId}/amount")
	public double getAmountByUserId(@PathVariable(value="userId") String userId);
    
	@RequestMapping(value = "/accounts/{userId}/amount", method = RequestMethod.PUT)
	public void setAmountForUserId(@PathVariable(value="userId") String userId, @RequestParam(value="amount") double amount);
	
	@RequestMapping(value = "/accounts/{userId}/amount", method = RequestMethod.PATCH)
	public void changeAmountForUserId(@PathVariable(value="userId") String userId, @RequestParam(value="amount") double amount);
	
	@RequestMapping(value = "/accounts/{userId}/tpc/amount", method = RequestMethod.PATCH)
	public void changeAmountForUserId4TPC(@PathVariable(value="userId") String userId, @RequestParam(value="amount") double amount);
}
