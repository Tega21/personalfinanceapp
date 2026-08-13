package com.personalfinance.personalfinancetracker.repository;

import com.personalfinance.personalfinancetracker.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for RecurringTransaction entity.
 * Provides CRUD operations and user-scoped queries for
 * recurring transaction template management.
 */
@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    /**
     * Retrieves all recurring transaction templates belonging to a specific user.
     *
     * @param userId the ID of the authenticated user
     * @return list of recurring transactions owned by the user
     */
    List<RecurringTransaction> findByUser_Id(Long userId);
}