/**
 * 
 */
package com.zerobase.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.zerobase.account.domain.Account;
import com.zerobase.account.type.AccountStatus;

@SpringBootTest
class AccountServiceTest {
	@Autowired
	private AccountService accountService;

	@BeforeEach
	void init() {
		accountService.createAccount();
	}

	@Test
	void testAccount() {
		Account account = accountService.getAccount(1L);

		assertEquals("40000", account.getAccountNumber());
		assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
	}

	@Test
	void testAccount2() {
		Account account = accountService.getAccount(2L);

		assertEquals("40000", account.getAccountNumber());
		assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
	}

	@Test
	void testAccount3() {
		Account account = accountService.getAccount(3L);

		assertEquals("40000", account.getAccountNumber());
		assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
	}

}
