package net.xdevelop.template.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.xdevelop.template.bank.service.IAlphaAccountService;
import net.xdevelop.template.bank.service.IBravoAccountService;
import net.xdevelop.tpc.TPCTransactionManager;

@Api(value = "Account service")
@RestController
@RequestMapping("/accounts")
public class AccountController {
	private enum Bank {
		ALL("ALL"),
		ALPHA("ALPHA"), 
		BRAVO("BRAVO");
		
		private String name;
		private Bank(String name) {
			this.name = name;
		}
		
		@Override
        public String toString() {
            return name;
        }
	}
	
	@Autowired
	private IAlphaAccountService alphaAccountService;
	
	@Autowired
	private IBravoAccountService bravoAccountService;
	
	@Autowired
	private TPCTransactionManager tm;
	
	@ApiOperation(value = "Get account amount by userId of specified bank", nickname = "getAmountByUserId", notes = "", 
			response = Double.class)
	@GetMapping("/{userId}/amount")
	public ResponseEntity<Double> getAmountByUserId(
			@ApiParam(value = "bank name", required = true, allowableValues = "ALPHA, BRAVO, ALL") @RequestParam String bank,
			@ApiParam(value = "User id", required = true) @PathVariable String userId) {
		
		double amount = 0.0;
		if (Bank.ALPHA.toString().equalsIgnoreCase(bank)) {
			amount = alphaAccountService.getAmountByUserId(userId);
		}
		else if (Bank.BRAVO.toString().equalsIgnoreCase(bank)) {
			amount = bravoAccountService.getAmountByUserId(userId);
		}
		else if (Bank.ALL.toString().equalsIgnoreCase(bank)) {
			amount = alphaAccountService.getAmountByUserId(userId) + bravoAccountService.getAmountByUserId(userId);
		}
		return ResponseEntity.ok(amount);
	}
	
	@ApiOperation(value = "Set amount for account of specified bank", nickname = "setAmountForUserId", notes = "")
	@PutMapping("/{userId}/amount")
	public ResponseEntity<String> setAmountForUserId(
			@ApiParam(value = "bank name", required = true, allowableValues = "ALPHA, BRAVO") @RequestParam String bank,
			@ApiParam(value = "User id", required = true) @PathVariable String userId, 
			@ApiParam(value = "amount", required = true) @RequestParam double amount) {
		
		if (Bank.ALPHA.toString().equalsIgnoreCase(bank)) {
			alphaAccountService.setAmountForUserId(userId, amount);
		}
		else if (Bank.BRAVO.toString().equalsIgnoreCase(bank)) {
			bravoAccountService.setAmountForUserId(userId, amount);
		}
		return ResponseEntity.ok(String.format("Set amount %f for account %s of %s bank successfully", amount, userId, bank));
	}
	
	@ApiOperation(value = "Transfer money between bank for same account", nickname = "transfer", notes = "")
	@PostMapping("/{userId}/transfer")
	public ResponseEntity<String> transfer(
			@ApiParam(value = "bank name transfer from", required = true, allowableValues = "ALPHA, BRAVO") @RequestParam String bankTransferFrom,
			@ApiParam(value = "bank name transfer in", required = true, allowableValues = "ALPHA, BRAVO") @RequestParam String bankTransferTo,
			@ApiParam(value = "User id", required = true) @PathVariable String userId, 
			@ApiParam(value = "amount", required = true) @RequestParam double amount) {
		
		if (amount <= 0) {
			throw new RuntimeException("Invalid amount");
		}
		
		if (Bank.ALPHA.toString().equalsIgnoreCase(bankTransferFrom) 
				&& Bank.BRAVO.toString().equalsIgnoreCase(bankTransferTo)) {
			alphaAccountService.changeAmountForUserId(userId, -amount);
			bravoAccountService.changeAmountForUserId(userId, amount);
		}
		else if (Bank.ALPHA.toString().equalsIgnoreCase(bankTransferFrom) 
				&& Bank.BRAVO.toString().equalsIgnoreCase(bankTransferTo)) {
			bravoAccountService.changeAmountForUserId(userId, -amount);
			alphaAccountService.changeAmountForUserId(userId, amount);
		}
		else {
			throw new RuntimeException("Invalid bank name");
		}
		return ResponseEntity.ok(String.format("Transfer %s bank to %s bank successfully", bankTransferFrom, bankTransferTo));
	}
	
	@ApiOperation(value = "Transfer money between bank for same account (transaction verion)", nickname = "tpcTransfer", notes = "")
	@PostMapping("/{userId}/tpc/transfer")
	public ResponseEntity<String> tpcTransfer(
			@ApiParam(value = "bank name transfer from", required = true, allowableValues = "ALPHA, BRAVO") @RequestParam String bankTransferFrom,
			@ApiParam(value = "bank name transfer in", required = true, allowableValues = "ALPHA, BRAVO") @RequestParam String bankTransferTo,
			@ApiParam(value = "User id", required = true) @PathVariable String userId, 
			@ApiParam(value = "amount", required = true) @RequestParam double amount) {
		
		if (amount <= 0) {
			throw new RuntimeException("Invalid amount");
		}
		
		boolean isSuccess = false;
		String err = "";
		if (Bank.ALPHA.toString().equalsIgnoreCase(bankTransferFrom) 
				&& Bank.BRAVO.toString().equalsIgnoreCase(bankTransferTo)) {
			
			try {
				tm.timeout(5).begin();
				alphaAccountService.changeAmountForUserId4TPC(userId, -amount);
				bravoAccountService.changeAmountForUserId4TPC(userId, amount);
				tm.commit(IAlphaAccountService.class, IBravoAccountService.class);
				isSuccess = true;
			} catch (Exception e) {
				tm.rollback(e, IAlphaAccountService.class, IBravoAccountService.class);
				err = e.getMessage();
			}
		}
		else if (Bank.ALPHA.toString().equalsIgnoreCase(bankTransferFrom) 
				&& Bank.BRAVO.toString().equalsIgnoreCase(bankTransferTo)) {
			
			try {
				tm.timeout(5).begin();
				bravoAccountService.changeAmountForUserId4TPC(userId, -amount);
				alphaAccountService.changeAmountForUserId4TPC(userId, amount);
				tm.commit(IAlphaAccountService.class, IBravoAccountService.class);
				isSuccess = true;
			} catch (Exception e) {
				tm.rollback(e, IAlphaAccountService.class, IBravoAccountService.class);
				err = e.getMessage();
			}
		}
		else {
			throw new RuntimeException("Invalid bank name");
		}
		
		if (isSuccess) {
			return ResponseEntity.ok(String.format("Transfer %s bank to %s bank successfully", bankTransferFrom, bankTransferTo));
		}
		else {
			throw new RuntimeException(String.format("Transfer %s bank to %s bank failed: %s", bankTransferFrom, bankTransferTo, err));
		}
	}
}
