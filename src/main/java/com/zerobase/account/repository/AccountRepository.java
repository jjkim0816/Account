package com.zerobase.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findFirstByOrderByIdDesc();
	
	Integer countByAccountUser(AccountUser AccountUser);

	Optional<Account> findByAccountNumber(String AccountNumber);

	List<Account> findByAccountUser(AccountUser AccountUser);
}
