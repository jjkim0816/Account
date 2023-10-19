package com.zerobase.account.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerobase.account.domain.Account;
import com.zerobase.account.dto.AccountInfo;
import com.zerobase.account.dto.CreateAccount;
import com.zerobase.account.dto.DeleteAccount;
import com.zerobase.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;
	
	@GetMapping("/account")
	public List<AccountInfo> getAccountByUserId(
		@RequestParam("user_id") Long userId
	) {
		return accountService.getAccountsByUserId(userId)
					.stream()
					.map(accountDto -> AccountInfo.builder()
							.accountNumber(accountDto.getAccountNumber())
							.balance(accountDto.getBalance())
							.build())
					.collect(Collectors.toList());
	}
	
	@GetMapping("/account/{id}")
	public Account getAccount(@PathVariable Long id) {
		return accountService.getAccount(id);
	}

	@PostMapping("/account")
	public CreateAccount.Response createAccount(
			@RequestBody @Valid CreateAccount.Request request) {
		return CreateAccount.Response.from(
				accountService.createAccount(
						request.getUserId(),
						request.getInitialBalance())
		);
	}
	
	@DeleteMapping("/account")
	public DeleteAccount.Response deleteAccount (
			@RequestBody @Valid DeleteAccount.Request request
	) {
		return DeleteAccount.Response.from(
				accountService.deleteAccount(
				    request.getUserId(),
					request.getAccountNumber()
				)
		);
	}
}
