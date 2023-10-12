package com.zerobase.account.service;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;
import com.zerobase.account.dto.AccountDto;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.repository.AccountUserRepository;
import com.zerobase.account.type.AccountStatus;
import com.zerobase.account.type.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;
	private final AccountUserRepository accountUserRepository;

	/**
	 * 사용자 존재 여부 확인, 계좌번호 생성, 계좌 정보 저장, 저장된 정보 응답
	 */
	@Transactional
	public AccountDto createAccount(Long userId, Long initialBalance) {
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(
						() -> new AccountException(ErrorCode.USER_NOT_FOUND));

		String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
				.map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
				.orElse("1000000000");

		return AccountDto.fromEntity(accountRepository.save(Account.builder()
				.accountUser(accountUser)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber(newAccountNumber)
				.balance(initialBalance)
				.registeredAt(LocalDateTime.now())
				.build())
		);
	}

	@Transactional
	public Account getAccount(Long id) {
		return accountRepository.findById(id).get();
	}
}
