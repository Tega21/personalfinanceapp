package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDate transactionDate;
    private String description;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;
}
