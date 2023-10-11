package com.zerobase.account.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.zerobase.account.type.AccountStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account {
	@Id
	@GeneratedValue
	Long id;

	private String accountNumber;

	@Enumerated(EnumType.STRING)
	private AccountStatus accountStatus;
}
