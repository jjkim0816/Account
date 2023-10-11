package com.zerobase.account.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.zerobase.account.domain.Account;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.type.AccountStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;

	@Transactional
	public void createAccount() {
		Account account = Account.builder().accountNumber("40000")
				.accountStatus(AccountStatus.IN_USE).build();
		accountRepository.save(account);
	}

	@Transactional
	public Account getAccount(Long id) {
		return accountRepository.findById(id).get();
	}
}
