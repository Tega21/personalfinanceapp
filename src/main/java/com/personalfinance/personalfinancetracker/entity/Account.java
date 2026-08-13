package com.personalfinance.personalfinancetracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a financial account belonging to a user.
 * Accounts are manually managed and the balance is set and updated by the user
 * rather than calculated from transactions for ethical reasons.
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    /**
     * The user who owns this account.
     * Cascade delete ensures accounts are removed when the user is deleted.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Display name for the account (e.g., "Wells Fargo Checking"). */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Account type. Determines how it is displayed and categorized.
     * Will be String in DB
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    /** Current balance, manually set by the user. Defaults to zero. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /** Name of the bank or institution (optional). */
    @Column(length = 100)
    private String institution;

    /** Enum for supported account types. */
    public enum AccountType {
        CHECKING,
        SAVINGS,
        OTHER
    }
}