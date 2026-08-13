package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.Account;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO returned to the client for account data.
 * Excludes the full User object to avoid circular references.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {

    /** Unique identifier for the account. */
    private Long accountId;

    /** Display name for the account. */
    private String name;

    /** Account type — CHECKING, SAVINGS, or OTHER. */
    private Account.AccountType type;

    /** Current manually set balance. */
    private BigDecimal balance;

    /** Optional institution name. */
    private String institution;
}