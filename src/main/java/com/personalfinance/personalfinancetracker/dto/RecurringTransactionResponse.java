package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.RecurringTransaction;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO returned to the client for recurring transaction template data.
 * Excludes the full User object to avoid circular references.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecurringTransactionResponse {

    /** Unique identifier for the recurring transaction template. */
    private Long id;

    /** Description of the recurring transaction. */
    private String description;

    /** Amount of the recurring transaction. */
    private BigDecimal amount;

    /** Transaction type — INCOME or EXPENSE. */
    private RecurringTransaction.TransactionType type;

    /** ID of the associated category. */
    private Long categoryId;

    /** Name of the associated category. */
    private String categoryName;

    /** Day of the month this transaction recurs. */
    private Integer dayOfMonth;
}