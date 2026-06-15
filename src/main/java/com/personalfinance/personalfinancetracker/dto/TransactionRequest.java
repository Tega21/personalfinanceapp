package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotNull(message = "Amountt is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String description;
}
