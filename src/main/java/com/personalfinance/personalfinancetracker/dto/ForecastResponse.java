package com.personalfinance.personalfinancetracker.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO returned to the client for a cash flow forecast.
 * Contains projected income, expenses, and net balance for a given
 * number of days based on the user's recurring transaction templates.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForecastResponse {

    /** Number of days this forecast covers (30, 60, or 90). */
    private int days;

    /** Total projected income within the forecast window. */
    private BigDecimal projectedIncome;

    /** Total projected expenses within the forecast window. */
    private BigDecimal projectedExpenses;

    /**
     * Projected net balance — projectedIncome minus projectedExpenses.
     * Positive means net income, negative means net expense.
     */
    private BigDecimal projectedNet;

    /** Individual recurring transactions that fall within the forecast window. */
    private List<RecurringTransactionResponse> upcomingTransactions;
}