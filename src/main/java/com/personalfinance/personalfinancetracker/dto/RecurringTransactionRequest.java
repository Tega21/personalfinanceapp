package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.RecurringTransaction;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a recurring transaction template.
 */
@Data
public class RecurringTransactionRequest {

    /** Description of the recurring transaction (e.g., "Netflix", "Rent"). */
    @NotBlank(message = "Description is required")
    private String description;

    /** Amount of the recurring transaction. Must be greater than zero. */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    /** Transaction type — INCOME or EXPENSE. */
    @NotNull(message = "Type is required")
    private RecurringTransaction.TransactionType type;

    /** Category ID to associate this recurring transaction with. */
    @NotNull(message = "Category is required")
    private Long categoryId;

    /** Day of the month this transaction recurs (1-31). */
    @NotNull(message = "Day of month is required")
    @Min(value = 1, message = "Day of month must be between 1 and 31")
    @Max(value = 31, message = "Day of month must be between 1 and 31")
    private Integer dayOfMonth;
}