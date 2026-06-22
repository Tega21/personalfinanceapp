package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response payload representing a category, returned by
 * POST /api/categories (new created category) and also
 * GET /api/categories (user's full category list).
 */
@Data
@Builder
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private String description;
}