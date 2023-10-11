package com.zerobase.Account;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AccountDtoTest {

	@Test
	void accountDto() {
		// given

		// when

		// then

		AccountDto accountDto = new AccountDto("accountNumber", "summer",
				LocalDateTime.now());
		System.out.println(accountDto.getAccountNumber());
		System.out.println(accountDto.toString());
		accountDto.log();

		System.out.println(NumberUtil.sum(1, 2));
		System.out.println(NumberUtil.minus(1, 2));
	}

}
