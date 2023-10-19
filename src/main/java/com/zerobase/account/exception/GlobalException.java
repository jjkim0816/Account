package com.zerobase.account.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.zerobase.account.dto.ErrorResponse;
import com.zerobase.account.type.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalException {
	@ExceptionHandler(AccountException.class)
	public ErrorResponse handleAccountException(AccountException e) {
		log.error("{} is occured", e.getErrorCode());
		return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ErrorResponse handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
		log.error("MethodArgumentNotValidException: " + e);
		
		return new ErrorResponse(
			ErrorCode.INVALID_REQUEST,
			ErrorCode.INVALID_REQUEST.getDescription()
		);
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class) 
	public ErrorResponse handleDateIntegerityViolationException(
			DataIntegrityViolationException e) {
		log.error("DataIntegrityViolationException : " + e);
		
		return new ErrorResponse(
			ErrorCode.INVALID_REQUEST,
			ErrorCode.INVALID_REQUEST.getDescription()
		);
	}

	@ExceptionHandler(Exception.class)
	public ErrorResponse handleException(Exception e) {
		log.error("Exception is occured: " + e);
		return new ErrorResponse(
			ErrorCode.INVALID_SERVER_ERROR,
			ErrorCode.INVALID_SERVER_ERROR.getDescription()
		);
	}
}
