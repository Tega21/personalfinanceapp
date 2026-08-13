package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a financial account.
 * The balance is manually set by the user rather than calculated from transactions.
 */
@Data
public class AccountRequest {

    /** Display name for the account (e.g., "Chase Checking"). */
    @NotBlank(message = "Account name is required")
    private String name;

    /** Account type — CHECKING, SAVINGS, or OTHER. */
    @NotNull(message = "Account type is required")
    private Account.AccountType type;

    /** Manually set balance. Must be zero or greater. */
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;

    /** Optional institution name (e.g., "Chase", "Wells Fargo"). */
    private String institution;
}