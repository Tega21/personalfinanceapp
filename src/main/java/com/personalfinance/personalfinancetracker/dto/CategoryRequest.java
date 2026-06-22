package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for creating a new category.
 * Used by POST /api/category. Name and type are needed.
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;

    private String description;
}