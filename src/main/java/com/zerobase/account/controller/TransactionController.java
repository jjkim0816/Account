package com.zerobase.account.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.zerobase.account.aop.AccountLock;
import com.zerobase.account.dto.CancelBalance;
import com.zerobase.account.dto.QueryTransactionResponse;
import com.zerobase.account.dto.UseBalance;
import com.zerobase.account.exception.AccountException;
import com.zerobase.account.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
	private final TransactionService transactionService;
	
	@GetMapping("/transaction/{transactionId}")
	public QueryTransactionResponse queryTransaction(
		@PathVariable String transactionId
	) {
		return QueryTransactionResponse.from(
			transactionService.queryTransactionId(transactionId)
		);
	}
	
	@PostMapping("/transaction/use")
	@AccountLock
	public UseBalance.Response useBalance(
		@Valid @RequestBody UseBalance.Request request
	) throws InterruptedException {
		try {
			Thread.sleep(3000L); // 동시성 테스트를 위해 삽입 
			return UseBalance.Response.from(transactionService.useBalance(request.getUserId()
					, request.getAccountNumber(), request.getAmount()));
		} catch (AccountException e) {
			log.error("Failed to use balance.");
			
			transactionService.saveFailedUseTransaction(
				request.getAccountNumber(),
				request.getAmount()
			);

			throw e;
		}
	}
	
	/**
	 * 거래 취소
	 * 부분 취소, 중복 취소 로직은 고려하지 않음 
	 * @param request
	 * @return
	 */
	@PostMapping("/transaction/cancel")
	@AccountLock
	public CancelBalance.Response cancelBalance(
		@Valid @RequestBody CancelBalance.Request request
	) {
		try {
			return CancelBalance.Response.from(
					transactionService.cancelBalance(
							request.getTransactionId(),
							request.getAccountNumber(),
							request.getAmount())
			);
		} catch (AccountException e) {
			log.error("Failed to use balance");
			
			transactionService.saveFailedCancelTransaction(
				request.getAccountNumber(),
				request.getAmount()
			);
			
			throw e;
		}
	}
}
