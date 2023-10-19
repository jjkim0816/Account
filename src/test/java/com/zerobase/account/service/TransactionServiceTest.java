package com.zerobase.account.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
	public static final long USE_AMOUNT = 200L;
	public static final long CANCEL_AMOUNT = 200L;
	
	@Mock
	private TransactionRepository transactionRepository;
	
	@Mock
	private AccountUserRepository accountUserRepository;
	
	@Mock
	private AccountRepository accountRepository;
	
	@InjectMocks
	private TransactionService transactionService;

	@Test
	void successUseBalance() {
		// given
		AccountUser user = AccountUser.builder()
					.id(12L)
					.name("Pobi").build();
		
		Account account = Account.builder()
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000012")
				.build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
		
		given(transactionRepository.save(any()))
			.willReturn(Transaction.builder()
					.account(account)
					.transactionType(TransactionType.USE)
					.transactionResultType(TransactionResultType.S)
					.transactionId("transactionId")
					.transactedAt(LocalDateTime.now())
					.amount(1000L)
					.balanceSnapshot(9000L)
					.build());
		
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

		// when
		TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", 800L);
		
		// then
		verify(transactionRepository, times(1)).save(captor.capture());
		assertEquals(800L, captor.getValue().getAmount());
		assertEquals(9200L, captor.getValue().getBalanceSnapshot());

		assertEquals(9000L, transactionDto.getBalanceSnapshot());
		assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
		assertEquals(TransactionType.USE, transactionDto.getTransactionType());
		assertEquals(1000L, transactionDto.getAmount());
	}

	@Test
	@DisplayName("해당 유저 없음 - 잔액 사용 실패")
	void useBalance_userNotFound() {
		// given
		given(accountUserRepository.findById(anyLong()))
		.willReturn(Optional.empty());
		
		// when
		AccountException execption = assertThrows(AccountException.class, 
			() -> transactionService.useBalance(1L, "1000000000", 10000L));
		
		// then
		assertEquals(ErrorCode.USER_NOT_FOUND, execption.getErrorCode());
	}
	
	@Test
	@DisplayName("해당 계좌 없음 - 잔액 사용 실패")
	void useBalance_accountNotFound() {
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
				() -> transactionService.useBalance(1L,	"1000000000", 10000L));
		
		// then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
	void useBalance_userUnMatch () {
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
				() -> transactionService.useBalance(1L, "1000000000", 10000L));

		// then
		assertEquals(ErrorCode.USER_ACCOUNT_UNMATCH, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("해지 계좌는 사용할 수 없다.")
	void useBalance_alreadyUnregistered() {
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
							.accountNumber("1234567890")
							.build()));
		// when
		AccountException exception = assertThrows(AccountException.class, 
				() -> transactionService.useBalance(1L, "1234567890", 10000L));
		
		// then
		assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("거래 금액이 잔액보다 큰 경우")
	void useBalance_amountExceedBalance() {
		// given
		AccountUser user  = AccountUser.builder()
								.id(12L)
								.name("Pobi").build();
		
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		
		given(accountRepository.findByAccountNumber(anyString()))
		.willReturn(Optional.of(Account.builder()
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(100L)
				.accountNumber("1000000012")
				.build()));

		// when
		AccountException exception = assertThrows(AccountException.class, 
				() -> transactionService.useBalance(1L, "1234567890", 1000L));

		// then
		assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("실패 트랜잭션 저장")
	void saveFailedUseTransaction() {
		// given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		Account account = Account.builder()
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000012")
				.build();
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
		
		given(transactionRepository.save(any()))
		.willReturn(Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(1000L)
				.balanceSnapshot(9000L)
				.build());
		
		
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

		// when
		transactionService.saveFailedUseTransaction("1000000000", USE_AMOUNT);

		// then
		verify(transactionRepository, times(1)).save(captor.capture());
		assertEquals(USE_AMOUNT, captor.getValue().getAmount());
		assertEquals(10000L, captor.getValue().getBalanceSnapshot());
		assertEquals(TransactionResultType.F, captor.getValue().getTransactionResultType());
	}
	
	@Test
	void successCancelBalance() {
		// given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		Account account = Account.builder()
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000012")
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(9000L)
				.build();
		
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
		
		given(transactionRepository.save(any()))
			.willReturn(Transaction.builder()
					.account(account)
					.transactionType(TransactionType.CANCEL)
					.transactionResultType(TransactionResultType.S)
					.transactionId("transactionIdForCancel")
					.transactedAt(LocalDateTime.now())
					.amount(CANCEL_AMOUNT)
					.balanceSnapshot(10000L)
					.build());
		
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
		
		// when
		TransactionDto transactionDto = transactionService.cancelBalance(
				"transactionId",
				"1000000000",
				CANCEL_AMOUNT); 

		// then
		verify(transactionRepository, times(1)).save(captor.capture());
		assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
		assertEquals(10000L + CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot());

		assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
		assertEquals(TransactionType.CANCEL, transactionDto.getTransactionType());
		assertEquals(10000L, transactionDto.getBalanceSnapshot());
		assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
	}
	
	@Test
	@DisplayName("원 사용 거래 없음 - 잔액 사용 취소 실패")
	void cancelTransaction_transactionNotFound() {
		// given
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.empty());

		// when
		AccountException exception = assertThrows(AccountException.class, 
			() -> transactionService.cancelBalance("transactionId", "1000000000", 10000L));

		// then
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("해당 계좌 없음 - 잔액 사용 취소 실패")
	void cancelTransaction_accountNotFound() {
		// given
		Transaction transaction = Transaction.builder().build();
		
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.empty());
		
		// when
		AccountException exception = assertThrows(AccountException.class, 
			() -> transactionService.cancelBalance("transactionId", "1000000000", 10000L));

		// then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("거래와 계좌가 매칭 실패 - 잔액 사용 취소 실패")
	void cancelTransaction_transactionAccountUnMatch() {
		// given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		Account account = Account.builder()
				.id(1L)
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000000")
				.balance(1000L)
				.build();

		Account accountNotUse = Account.builder()
				.id(2L)
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000001")
				.balance(1000L)
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(9000L)
				.build();
		
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(accountNotUse));
		
		
		// when
		AccountException exception = assertThrows(AccountException.class, 
			() -> transactionService.cancelBalance(
					"transactionId",
					"1000000000",
					CANCEL_AMOUNT));
		
		// then
		assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("거래금액과 최소금액이 다름 - 잔액 사용 취소 실패")
	void cancelTransaction_cancelMustFully() {
		// given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		Account account = Account.builder()
				.id(1L)
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000012")
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT + 1000L)
				.balanceSnapshot(9000L)
				.build();

		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));

		// when
		AccountException exception = assertThrows(AccountException.class,
			() -> transactionService.cancelBalance(
					"transactionId",
					"1000000000",
					CANCEL_AMOUNT)
		);
		
		// then
		assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("취소는 1년 거래까지만 가능 - 잔액 사용 취소 실패")
	void cancelTransaction_tooOldOrderToCancel() {
		// given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("Pobi").build();
		
		Account account = Account.builder()
				.id(1L)
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000012")
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now().minusYears(1))
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(9000L)
				.build();

		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));

		// when
		AccountException exception = assertThrows(AccountException.class,
			() -> transactionService.cancelBalance(
					"transactionId",
					"1000000000",
					CANCEL_AMOUNT)
		);
		
		// then
		assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, exception.getErrorCode());
	}
}
	