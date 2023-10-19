package com.zerobase.account.controller;

//import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.account.domain.Account;
import com.zerobase.account.dto.AccountDto;
import com.zerobase.account.dto.CreateAccount;
import com.zerobase.account.dto.DeleteAccount;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.service.AccountService;
import com.zerobase.account.type.AccountStatus;
import com.zerobase.account.type.ErrorCode;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
	@MockBean
	private AccountService accountService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void successCreateAccount() throws Exception {
		// given
		given(accountService.createAccount(anyLong(), anyLong()))
			.willReturn(AccountDto.builder()
					.userId(1L)
					.accountNumber("1234567890")
					.registeredAt(LocalDateTime.now())
					.unRegisteredAt(LocalDateTime.now())
   				    .build());
		
		// when
		// then
		mockMvc.perform(post("/account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new CreateAccount.Request(3333L, 1111L)
				)))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.userId").value(1))
		.andExpect(jsonPath("$.accountNumber").value("1234567890"));
	}
	
	@Test
	void successDeleteAccount() throws JsonProcessingException, Exception {
		// given
		given(accountService.deleteAccount(anyLong(), anyString()))
				.willReturn(AccountDto.builder()
						.userId(1L)
						.accountNumber("1234567890")
						.registeredAt(LocalDateTime.now())
						.unRegisteredAt(LocalDateTime.now())
						.build());
		// when
		// then
		mockMvc.perform(delete("/account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new DeleteAccount.Request(3333L, "0987654321")
				)))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.userId").value(1))
		.andExpect(jsonPath("$.accountNumber").value("1234567890"));
	}

	@Test
	void successGetAccount () throws Exception {
		// given
		given(accountService.getAccount(anyLong()))
				.willReturn(Account.builder()
				.accountNumber("3456")
				.accountStatus(AccountStatus.IN_USE)
				.build());
		
		// when
		// then
		mockMvc.perform(get("/account/867"))
				.andDo(print())
				.andExpect(jsonPath("$.accountNumber").value("3456"))
				.andExpect(jsonPath("$.accountStatus").value("IN_USE"))
				.andExpect(status().isOk());
	}
	
	@Test
	@DisplayName("유저 계좌 목록 조회")
	void successGetAccountByUserId() throws Exception {
		// given
		List<AccountDto> accountDtos = Arrays.asList(
								   		AccountDto.builder()
												.accountNumber("1234567890")
												.balance(1000L)
												.build(),
								   		AccountDto.builder()
												.accountNumber("1234567891")
												.balance(2000L)
												.build(),
								   		AccountDto.builder()
												.accountNumber("1234567892")
												.balance(3000L)
												.build()
									);
		
		
		given(accountService.getAccountsByUserId(anyLong()))
			.willReturn(accountDtos);
		
		// when
		// then
		mockMvc.perform(get("/account?user_id=1"))
			.andDo(print())
			.andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
			.andExpect(jsonPath("$[0].balance").value(1000))
			.andExpect(jsonPath("$[1].accountNumber").value("1234567891"))
			.andExpect(jsonPath("$[1].balance").value(2000))
			.andExpect(jsonPath("$[2].accountNumber").value("1234567892"))
			.andExpect(jsonPath("$[2].balance").value(3000));
	}

	@Test
	void failGetAccount() throws Exception {
		// given
		given(accountService.getAccount(anyLong()))
			.willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
		// when
		// then
		mockMvc.perform(get("/account/876"))
			.andDo(print())
			.andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"))
			.andExpect(jsonPath("$.errorMessage").value("계좌가 없습니다."))
			.andExpect(status().isOk());
	}
}
