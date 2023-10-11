package com.zerobase.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerobase.account.domain.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
