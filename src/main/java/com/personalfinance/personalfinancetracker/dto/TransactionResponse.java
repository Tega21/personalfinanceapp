package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response payload representing a single transaction, returned by
 * the transaction CRUD endpoints and is embedded in Dashboard's recent
 * transactions list. It flattens the category relationship to
 * categoryName/categoryId instead of nesting full Category object.
 * Keeps the payload simple for the frontend.
 */
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
