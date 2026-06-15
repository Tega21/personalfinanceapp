package com.personalfinance.personalfinancetracker.repository;

import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.Transaction;
import com.personalfinance.personalfinancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    List<Transaction> findByCategoryIdAndUserIdOrderByTransactionDateDesc(Long categoryId, Long userId);
}