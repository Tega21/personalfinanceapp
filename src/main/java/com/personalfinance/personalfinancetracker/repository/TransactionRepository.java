package com.personalfinance.personalfinancetracker.repository;

import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.Transaction;
import com.personalfinance.personalfinancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.personalfinance.personalfinancetracker.dto.CategoryBreakdown;
import com.personalfinance.personalfinancetracker.entity.TransactionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.*;

/**
 * Repository for Transaction entities. Includes both simple derived
 * queries for CRUD/listing, and custom JPQL queries that power the
 * Dashboard's aggregated totals and category breakdown.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    List<Transaction> findByCategoryIdAndUserIdOrderByTransactionDateDesc(Long categoryId, Long userId);
    List<Transaction> findTop5ByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Sums all transactions of a given type (INCOME or EXPENSE) for a user
     * within a date range. Uses COALESCE to return 0 instead of null when
     * there are no matching transactions, avoiding a NullPointerException
     * when mapping the result into a BigDecimal.
     *
     * @param userId the user to sum transactions for
     * @param type whether to sum INCOME or EXPENSE transactions
     * @param startDate the first day of the date range (inclusive)
     * @param endDate the last day of the date range (inclusive)
     * @return the summed total, or 0 if no matching transactions exist
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = :type " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Aggregates transaction totals by category for a user within a date
     * range, used to build the Dashboard's category breakdown pie chart.
     * Uses a JPQL constructor expression to build CategoryBreakdown DTOs
     * directly in the query.
     *
     * @param userId the user to aggregate transactions for
     * @param type whether to aggregate INCOME or EXPENSE transactions
     * @param startDate the first day of the date range (inclusive)
     * @param endDate the last day of the date range (inclusive)
     * @return one CategoryBreakdown per category with a matching transaction
     */
    @Query("SELECT new com.personalfinance.personalfinancetracker.dto.CategoryBreakdown(" +
            "t.category.id, t.category.name, SUM(t.amount)) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = :type " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category.id, t.category.name")
    List<CategoryBreakdown> findCategoryBreakdown(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Transaction> findTop5ByUserIdOrderByTransactionDateDesc(Long userId);
}