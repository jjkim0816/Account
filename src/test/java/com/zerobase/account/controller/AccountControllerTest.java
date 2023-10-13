package com.zerobase.account.controller;

import static org.junit.jupiter.api.Assertions.*;

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
import com.zerobase.account.service.AccountService;
import com.zerobase.account.service.RedisTestService;
import com.zerobase.account.type.AccountStatus;

import java.time.LocalDateTime;

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
	
	@MockBean
	private RedisTestService redisTestService;
	
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

}
