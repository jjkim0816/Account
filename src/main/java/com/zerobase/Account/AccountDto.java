package com.zerobase.Account;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class AccountDto {
	private String accountNumber;
	private String nickName;
	private LocalDateTime registeredAt;

	public void log() {
		log.error("error is occurred");
	}
}
