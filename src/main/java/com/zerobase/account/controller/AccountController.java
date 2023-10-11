package com.zerobase.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.zerobase.account.domain.Account;
import com.zerobase.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@GetMapping("/create-account")
	public String createAccount() {
		accountService.createAccount();
		return "success";
	}

	@GetMapping("/account/{id}")
	public Account getAccount(@PathVariable Long id) {
		return accountService.getAccount(id);
	}
}
