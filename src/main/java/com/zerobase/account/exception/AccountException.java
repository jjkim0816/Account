package com.zerobase.account.exception;

import com.zerobase.account.type.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class AccountException extends RuntimeException {
	private final ErrorCode errorCode;
	private final String errorMessage;

	public AccountException(ErrorCode errorCode) {
		this.errorCode = errorCode;
		this.errorMessage = errorCode.getDescription();
	}

}
