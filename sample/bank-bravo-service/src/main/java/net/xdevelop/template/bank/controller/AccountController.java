package net.xdevelop.template.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.xdevelop.template.bank.service.AccountService;

@Api(value = "Account service")
@RestController
@RequestMapping("/accounts")
public class AccountController {
	@Autowired
	private AccountService accountService;
	
	@ApiOperation(value = "Get account amount by userId", nickname = "getAmountByUserId", notes = "", 
			response = Double.class)
	@GetMapping("/{userId}/amount")
	public double getAmountByUserId(
			@ApiParam(value = "User id", required = true) @PathVariable String userId) {
		double amount = accountService.getAmountByUserId(userId);
		return amount;
	}
	
	@ApiOperation(value = "Set amount for account", nickname = "setAmountForUserId", notes = "")
	@PutMapping("/{userId}/amount")
	public void setAmountForUserId(@PathVariable String userId, @RequestParam double amount) {
		if (amount <= 0) {
			throw new RuntimeException("Invalid amount");
		}
		accountService.setAmountForUserId(userId, amount);
	}
	
	@ApiOperation(value = "Increase/decrease amount of account", nickname = "changeAmountForUserId", notes = "")
	@PatchMapping("/{userId}/amount")
	public void changeAmountForUserId(@PathVariable String userId, @RequestParam double amount) {
		if (amount == 0) {
			return;
		}
		else if (amount < 0) {
			accountService.transferOut(userId, -amount);
		}
		else if (amount > 0) {
			accountService.transferIn(userId, amount);
		}
	}
	
	@ApiOperation(value = "Increase/decrease amount of account (TPC transaction version)", nickname = "changeAmountForUserId4XA", notes = "")
	@PatchMapping("/{userId}/tpc/amount")
	public void changeAmountForUserId4TPC(@PathVariable String userId, @RequestParam double amount) throws Exception {
		if (amount == 0) {
			return;
		}
		else if (amount < 0) {
			accountService.transferOut4TPC(userId, -amount);
		}
		else if (amount > 0) {
			accountService.transferIn4TPC(userId, amount);
		}
	}
}
