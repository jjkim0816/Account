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
import com.zerobase.account.dto.TransactionDto;
import com.zerobase.account.dto.UseBalance;
import com.zerobase.account.service.TransactionService;
import com.zerobase.account.type.TransactionResultType;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
	@MockBean
	private TransactionService transactionService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void successUseBalance() throws JsonProcessingException, Exception {
		// given
		given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
		.willReturn(TransactionDto.builder()
				.accountNumber("1000000000")
				.transactedAt(LocalDateTime.now())
				.amount(12345L)
				.transactionId("transactionId")
				.transactionResultType(TransactionResultType.S)
				.build());
		
		// when
		// then
		mockMvc.perform(post("/transaction/use")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new UseBalance.Request(1L, "2000000000", 3000L)
				))
		)
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.accountNumber").value("1000000000"))
		.andExpect(jsonPath("$.amount").value(12345L))
		.andExpect(jsonPath("$.transactionId").value("transactionId"))
		.andExpect(jsonPath("$.transactionResult").value("S"));
	}

}
