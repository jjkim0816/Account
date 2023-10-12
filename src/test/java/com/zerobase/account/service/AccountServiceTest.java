/**
 * 
 */
package com.zerobase.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;
import com.zerobase.account.dto.AccountDto;
import com.zerobase.account.repository.AccountRepository;
import com.zerobase.account.repository.AccountUserRepository;

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
}
