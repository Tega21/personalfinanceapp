package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.ForecastResponse;
import com.personalfinance.personalfinancetracker.dto.RecurringTransactionResponse;
import com.personalfinance.personalfinancetracker.entity.RecurringTransaction;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.repository.RecurringTransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for cash flow forecasting.
 * Projects the user's recurring transaction templates forward across
 * a 30, 60, or 90 day window to calculate expected income, expenses,
 * and net balance for that period.
 */
@Service
@RequiredArgsConstructor
public class ForecastService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final UserRepository userRepository;

    /**
     * Generates a cash flow forecast for the authenticated user.
     * Iterates day by day through the forecast window and checks whether
     * each recurring transaction falls on that day of the month.
     *
     * @param username the username of the authenticated user
     * @param days     the number of days to forecast (30, 60, or 90)
     * @return a ForecastResponse containing projected totals and upcoming transactions
     */
    @Transactional(readOnly = true)
    public ForecastResponse getForecast(String username, int days) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<RecurringTransaction> recurringList =
                recurringTransactionRepository.findByUser_Id(user.getId());

        BigDecimal projectedIncome = BigDecimal.ZERO;
        BigDecimal projectedExpenses = BigDecimal.ZERO;
        List<RecurringTransactionResponse> upcoming = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        // Iterate through each day in the forecast window
        LocalDate current = today;
        while (!current.isAfter(endDate)) {
            final int dayOfMonth = current.getDayOfMonth();
            final LocalDate currentDate = current;

            for (RecurringTransaction rt : recurringList) {
                // Check if this recurring transaction fires on this day
                // Handle months shorter than the transaction's dayOfMonth
                int lastDayOfMonth = currentDate.lengthOfMonth();
                int effectiveDay = Math.min(rt.getDayOfMonth(), lastDayOfMonth);

                if (effectiveDay == dayOfMonth) {
                    RecurringTransactionResponse response = new RecurringTransactionResponse(
                            rt.getId(),
                            rt.getDescription(),
                            rt.getAmount(),
                            rt.getType(),
                            rt.getCategory().getId(),
                            rt.getCategory().getName(),
                            rt.getDayOfMonth()
                    );
                    upcoming.add(response);

                    if (rt.getType() == RecurringTransaction.TransactionType.INCOME) {
                        projectedIncome = projectedIncome.add(rt.getAmount());
                    } else {
                        projectedExpenses = projectedExpenses.add(rt.getAmount());
                    }
                }
            }
            current = current.plusDays(1);
        }

        BigDecimal projectedNet = projectedIncome.subtract(projectedExpenses);

        return new ForecastResponse(days, projectedIncome, projectedExpenses,
                projectedNet, upcoming);
    }
}