package com.zerobase.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	USER_NOT_FOUND("사용자가 없습니다."),
	MAX_COUNT_PER_USER_10("사용자 최대 계좌는 10개입니다.")
	;

	private String description;
}
