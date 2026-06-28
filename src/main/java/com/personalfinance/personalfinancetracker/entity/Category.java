package com.personalfinance.personalfinancetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a spending or income category a transaction can be assigned
 * to. Categories are scoped per user — each user gets their own set,
 * seeded automatically with 15 defaults at registration (isDefault = true),
 * plus any custom categories they create themselves (isDefault = false).
 * There is no protection preventing a user from deleting a default category.
 */

@Entity
@Table(name="categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private CategoryType categoryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;


    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
