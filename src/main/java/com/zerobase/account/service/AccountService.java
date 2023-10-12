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
		
		validateCreateAccount(accountUser);

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

	private void validateCreateAccount(AccountUser accountUser) {
		if(accountRepository.countByAccountUser(accountUser) == 10) {
			throw new AccountException(ErrorCode.MAX_COUNT_PER_USER_10);
		}
	}

	@Transactional
	public Account getAccount(Long id) {
		return accountRepository.findById(id).get();
	}

	@Transactional
	public AccountDto deleteAccount(Long userId, String accountNumber) {
		
		// 사용자가 없는 경우 
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(
						() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		
		// 계좌가 없는 경우 
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(
						() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
		
		validateDeleteAccount(accountUser, account);
		
		account.setAccountStatus(AccountStatus.UNREGISTERD);
		account.setUnRegisteredAt(LocalDateTime.now());
		
		return AccountDto.fromEntity(account);
	}

	private void validateDeleteAccount(AccountUser accountUser, Account account) {
		
		// 사용자 아이디와 계좌 소유주가 다른 경우  
		if(accountUser.getId() != account.getAccountUser().getId()) {
			throw new AccountException(ErrorCode.USER_ACCOUNT_UNMATCH);
		}
		
		// 계좌가 이미 해지된 상태 
		if(account.getAccountStatus() == AccountStatus.UNREGISTERD) {
			throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
		}
		
		// 계좌 잔액이 남은 경우 
		if(account.getBalance() > 0) {
			throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
		}
	}
}
