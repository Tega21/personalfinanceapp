package com.personalfinance.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponse {
    private Long budgetId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amountLimit;
    private Integer month;
    private Integer year;
    private BigDecimal spent;
    private BigDecimal percentUsed;
    private String status;


}
