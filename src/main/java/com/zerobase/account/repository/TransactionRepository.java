package com.zerobase.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zerobase.account.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{

}
