package com.zerobase.account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerobase.account.domain.Account;
import com.zerobase.account.domain.AccountUser;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findFirstByOrderByIdDesc();
	
	Integer countByAccountUser(AccountUser accountUser);
}
