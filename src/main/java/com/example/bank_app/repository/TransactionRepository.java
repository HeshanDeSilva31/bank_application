package com.example.bank_app.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bank_app.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTop10ByOrderByCreatedAtDesc();
    long countByStatus(Transaction.Status status);
    List<Transaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(String fromAccount, String toAccount);
    List<Transaction> findByStatusAndAmountGreaterThanOrderByCreatedAtDesc(
            Transaction.Status status, BigDecimal amount);
}
