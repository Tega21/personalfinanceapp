package com.personalfinance.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Representation of one slice of the dashboard's spending by
 * category pie chart. A single category's total spent/earned within
 * the requested date range
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreakdown {

    private Long categoryId;
    private String categoryName;
    private BigDecimal total;
}
