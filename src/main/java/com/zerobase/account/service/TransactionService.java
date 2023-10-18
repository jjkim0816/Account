package com.zerobase.account.service;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;
import com.zerobase.account.domain.Transaction;
import com.zerobase.account.dto.TransactionDto;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.repository.AccountUserRepository;
import com.zerobase.account.repository.TransactionRepository;
import com.zerobase.account.type.AccountStatus;
import com.zerobase.account.type.ErrorCode;
import com.zerobase.account.type.TransactionResultType;
import com.zerobase.account.type.TransactionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final AccountUserRepository accountUserRepository;
	private final AccountRepository accountRepository;
	
	@Transactional
	public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {
		AccountUser user = accountUserRepository.findById(userId)
			.orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		
		
		Account account = accountRepository.findByAccountNumber(accountNumber)
			.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

		validateUseBalance(user, account, amount);
		
		account.useBalance(amount);
		
		return TransactionDto.fromEntity(saveAndGetTransaction(TransactionResultType.S,
				amount,account));
	}

	private void validateUseBalance(AccountUser user, Account account, Long amount) {
		if (user.getId() != account.getAccountUser().getId()) {
			throw new AccountException(ErrorCode.USER_ACCOUNT_UNMATCH);
		}
		
		if (account.getAccountStatus() != AccountStatus.IN_USE) {
			throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
		}
		
		if (account.getBalance() < amount) {
			throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
		}
	}

	@Transactional
	public void saveFailedUseTransaction(String accountNumber, Long amount) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
			.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
		
		saveAndGetTransaction(TransactionResultType.F, amount, account);
	}

	private Transaction saveAndGetTransaction(
			TransactionResultType transactionResultType,
			Long amount,
			Account account) {
		return transactionRepository.save(
			Transaction.builder()
				.transactionType(TransactionType.USE)
				.transactionResultType(transactionResultType)
				.account(account)
				.amount(amount)
				.balanceSnapshot(account.getBalance())
				.transactionId(UUID.randomUUID().toString().replace("-", ""))
				.transactedAt(LocalDateTime.now())
				.build()
		);
	}

}
