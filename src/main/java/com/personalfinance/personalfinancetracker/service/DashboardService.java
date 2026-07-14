package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.CategoryBreakdown;
import com.personalfinance.personalfinancetracker.dto.DashboardSummary;
import com.personalfinance.personalfinancetracker.dto.TrendDataPoint;
import com.personalfinance.personalfinancetracker.entity.TransactionType;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.repository.TransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the aggregated financial summary that helps the Dashboard
 * screen like income/expense totals, net cash flow, a category breakdown
 * for the pie chart, and recent transactions. All scoped to a
 * requested month and year.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    /**
     * Retrieves the dashboard summary for a user for a given month and
     * year. Wrapped in a read-only transaction so the database session
     * stays open through the recent transactions mapping step, which
     * lazily loads each transaction's category.
     *
     * @param username the authenticated user's username
     * @param month the month to summarize (1-12)
     * @param year the year to summarize
     * @return the dashboard summary for the requested month
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary(String username, int month, int year) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal totalIncome = transactionRepository.sumByUserIdAndTypeAndDateRange(
                user.getId(), TransactionType.INCOME, startDate, endDate);

        BigDecimal totalExpenses = transactionRepository.sumByUserIdAndTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE, startDate, endDate);

        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);

        List<CategoryBreakdown> categoryBreakdown = transactionRepository.findCategoryBreakdown(
                user.getId(), TransactionType.EXPENSE, startDate, endDate);

        List<com.personalfinance.personalfinancetracker.dto.TransactionResponse> recentTransactions =
                transactionRepository.findTop5ByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                                user.getId(), startDate, endDate)
                        .stream()
                        .map(transactionService::mapToResponse)
                        .collect(Collectors.toList());

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netCashFlow(netCashFlow)
                .categoryBreakdown(categoryBreakdown)
                .recentTransactions(recentTransactions)
                .build();
    }

    /**
     * Builds a spending trend dataset for the last number of months,
     * used to power the Dashboard's line chart. Each entry represents one
     * calendar month with its total spending. Months are returned
     * in chronological order (oldest first) so the chart reads left to
     * right naturally. Months with no transactions return 0 instead of
     * being omitted, so the chart always shows a complete, unbroken line.
     *
     * @param username the authenticated user's username
     * @param months the number of past months to include (e.g., 6)
     * @return a list of monthly expense totals in chronological order
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    public List<TrendDataPoint> getSpendingTrends(String username, int months) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<TrendDataPoint> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            BigDecimal total = transactionRepository.sumExpensesByUserAndDateRange(
                    user.getId(), monthStart, monthEnd);

            String label = monthStart.getMonth().getDisplayName(
                    java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)
                    + " " + monthStart.getYear();

            trends.add(TrendDataPoint.builder()
                    .month(label)
                    .totalExpenses(total)
                    .build());
        }

        return trends;
    }
}