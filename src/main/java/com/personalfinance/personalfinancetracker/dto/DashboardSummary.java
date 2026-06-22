package com.personalfinance.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated summary of the financials for a given month, returned
 * by GET /api/dashboard/summary. Combines income/expense totals, a
 * per category spending breakdown for the pie chart, and user's most
 * recent transactions so that the frontend can render the whole Dash
 * screen from a single API call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netCashFlow;
    private List<CategoryBreakdown> categoryBreakdown;
    private List<TransactionResponse> recentTransactions;
}
