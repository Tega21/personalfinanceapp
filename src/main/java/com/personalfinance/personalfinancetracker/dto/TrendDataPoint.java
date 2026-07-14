package com.personalfinance.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Used to show a single month's total expense spending, used to build
 * the spending trend line chart on the Dashboard. The month field is
 * a label that's ready for direct display in the frontend chart.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrendDataPoint {

    private String month;
    private BigDecimal totalExpenses;

}
