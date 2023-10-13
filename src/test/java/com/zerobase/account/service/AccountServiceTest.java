/**
 * 
 */
package com.zerobase.account.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;
import com.zerobase.account.dto.AccountDto;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.repository.AccountUserRepository;
import com.zerobase.account.type.AccountStatus;
import com.zerobase.account.type.ErrorCode;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
	@Mock
	private AccountRepository accountRepository;
	
	@Mock
	private AccountUserRepository accountUserRepository;
	
	@InjectMocks
	private AccountService accountService;
	
	@Test
	void createAccountSuccess() {
		// given
		AccountUser user = AccountUser.builder()
					.id(12L)
					.name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		
		given(accountRepository.findFirstByOrderByIdDesc())
			.willReturn(Optional.of(Account.builder()
					.accountNumber("1000000012")
					.build()));
		
		given(accountRepository.save(any()))
			.willReturn(Account.builder()
					.accountUser(user)
					.accountNumber("1000000013")
					.build());
		
		ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class); 
		
		// when
		AccountDto accountDto = accountService.createAccount(1L, 1111L);
		
		// then
		verify(accountRepository, times(1)).save(accountCaptor.capture());
		assertEquals(12L, accountDto.getUserId());
//		assertEquals("1000000013", accountDto.getAccountNumber());
		assertEquals("1000000013", accountCaptor.getValue().getAccountNumber());
	}
	
	@Test
	void createFirstAccount() {
		// given
		AccountUser user = AccountUser.builder()
					.id(15L)
					.name("Kobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		
		given(accountRepository.findFirstByOrderByIdDesc())
			.willReturn(Optional.empty());
		
		given(accountRepository.save(any()))
			.willReturn(Account.builder()
					.accountUser(user)
					.accountNumber("1000000015")
					.build());
		ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
		
		// when
		AccountDto accountDto = accountService.createAccount(1L, 1000L);
		
		// then
		verify(accountRepository, times(1)).save(accountCaptor.capture());
		assertEquals(15L, accountDto.getUserId());
		assertEquals("1000000000", accountCaptor.getValue().getAccountNumber());
	}
	
	@Test
	@DisplayName("해당 유저 없음 - 계좌 생성 실패")
	void createAccount_userNotFound() {
		// given
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.empty());
		
		// when
		AccountException exception = assertThrows(AccountException.class,
				() -> accountService.createAccount(1L, 1000L));

		// then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("유저 당 최대 계좌는 10개")
	void createAccount_maxAccountIs10() {
		// given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		
		given(accountRepository.countByAccountUser(any()))
			.willReturn(10);

		// when
		AccountException exception = assertThrows(AccountException.class,
				() -> accountService.createAccount(1L, 1000L));
		
		// then
		assertEquals(ErrorCode.MAX_COUNT_PER_USER_10, exception.getErrorCode());
	}

	@Test
	void deleteAccountSuccess() {
		// given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(Account.builder()
					.accountUser(user)
					.balance(0L)
					.accountNumber("1000000012")
					.build()));
		
		// 클래스에 저장되는 데이터를 캡쳐 떠서 저장
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

		// when
		AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");

		// then
		verify(accountRepository, times(1)).save(captor.capture());
		assertEquals(12L, accountDto.getUserId());
		assertEquals("1000000012", captor.getValue().getAccountNumber());
		assertEquals(AccountStatus.UNREGISTERD, captor.getValue().getAccountStatus());
	}
	
	
	@Test
	@DisplayName("해당 유저 없음 - 계좌 해지 실패")
	void deleteAccount_userNotFound() {
		// given
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.empty());
		
		// when
		AccountException exception = assertThrows(AccountException.class,
				() -> accountService.deleteAccount(1L, "1234567890"));
		
		// then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("해당 계좌 없음 - 계좌 해지 실패")
	void deleteAccount_AccountNotFound() {
		// given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.empty());
		
		// when
		AccountException exception = assertThrows(AccountException.class, 
				() -> accountService.deleteAccount(1L, "1234567890"));
		
		// then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("계좌 소유주 다름")
	void deleteAccountFailed_userUnMatch() {
		// given
		AccountUser pobi = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		AccountUser herry = AccountUser.builder()
				.id(13L)
				.name("Herry").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(pobi));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(Account.builder()
					.accountUser(herry)
					.balance(0L)
					.build()));
		
		// when
		AccountException exception = assertThrows(AccountException.class, 
				() -> accountService.deleteAccount(1L, "1234567890"));
		
		// then
		assertEquals(ErrorCode.USER_ACCOUNT_UNMATCH, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("계좌 잔액이 남은 경우")
	void deleteAccountFailed_balanceNotEmpty() {
		// given
		AccountUser pobi = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(pobi));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(Account.builder()
					.accountUser(pobi)
					.balance(100L)
					.accountNumber("1000000012")
					.build()));
		
		// when
		AccountException exception = assertThrows(AccountException.class,
			() -> accountService.deleteAccount(1L, "1234567890"));
		
		// then
		assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("해지 계좌는 해지할 수 없다.")
	void deleteAccountFailed_alreadyUnregistred() {
		// given
		AccountUser pobi = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(pobi));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(Account.builder()
					.accountUser(pobi)
					.accountStatus(AccountStatus.UNREGISTERD)
					.balance(0L)
					.accountNumber("1000000012")
					.build()));
		
		// when
		AccountException exception = assertThrows(AccountException.class,
				() -> accountService.deleteAccount(1L, "1234567890"));
		
		// then
		assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
	}
	
	@Test
	void successGetAccountsByUserId() {
		// given
		AccountUser pobi = AccountUser.builder()
							.id(12L)
							.name("Pobi")
							.build();
		
		List<Account> accounts = Arrays.asList(
			Account.builder()
				.accountUser(pobi)
				.accountNumber("1111111111")
				.balance(1000L)
				.build(),
			Account.builder()
				.accountUser(pobi)
				.accountNumber("2222222222")
				.balance(2000L)
				.build(),
			Account.builder()
				.accountUser(pobi)
				.accountNumber("3333333333")
				.balance(3000L)
				.build()
		);
		
		given(accountUserRepository.findById(anyLong()))
		   	.willReturn(Optional.of(pobi));
		
		given(accountRepository.findByAccountUser(any()))
			.willReturn(accounts);

		// when
		List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);
		
		// then
		assertEquals(3, accountDtos.size());
		assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
		assertEquals(1000L, accountDtos.get(0).getBalance());
		assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
		assertEquals(2000L, accountDtos.get(1).getBalance());
		assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
		assertEquals(3000L, accountDtos.get(2).getBalance());
	}
	
	@Test
	@DisplayName("계좌 정보 가져오기 실패")
	void failedToGetAccounts() {
		// given
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.empty());
		
		// when
		AccountException exception = assertThrows(AccountException.class, 
				() -> accountService.getAccountsByUserId(1L));
		// then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}
	
}
