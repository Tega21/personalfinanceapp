package com.personalfinance.personalfinancetracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a recurring transaction template.
 * These are not actual transactions but templates used to project
 * future cash flow. The user defines bills and income that repeat
 * monthly, and the forecast service uses them to build projections.
 */
@Entity
@Table(name = "recurring_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransaction {

    /** Primary key, auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this recurring transaction template.
     * Cascade delete ensures templates are removed when the user is deleted.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The category this recurring transaction belongs to.
     * References the existing categories table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Description of the recurring transaction (e.g., "Netflix", "Rent"). */
    @Column(nullable = false, length = 255)
    private String description;

    /** Amount of the recurring transaction. Always positive. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Transaction type — INCOME or EXPENSE.
     * Stored as a string in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    /**
     * Day of the month this transaction recurs (1-31).
     * Used by the forecast service to project into future windows.
     */
    @Column(nullable = false)
    private Integer dayOfMonth;

    /** Enum for transaction type, consistent with the Transaction entity. */
    public enum TransactionType {
        INCOME,
        EXPENSE
    }
}