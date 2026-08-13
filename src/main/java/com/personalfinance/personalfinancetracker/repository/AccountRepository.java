package com.personalfinance.personalfinancetracker.repository;

import com.personalfinance.personalfinancetracker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Account entity.
 * Provides CRUD operations and custom queries for account management.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Retrieves all accounts belonging to a specific user.
     *
     * @param userId the ID of the authenticated user
     * @return list of accounts owned by the user
     */
    List<Account> findByUser_Id(Long userId);

    /**
     * Calculates the total net worth for a user by summing all account balances.
     *
     * @param userId the ID of the authenticated user
     * @return total balance across all accounts, or 0 if no accounts exist
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.id = :userId")
    BigDecimal sumBalanceByUserId(Long userId);
}