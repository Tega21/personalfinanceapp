package com.personalfinance.personalfinancetracker.dto;

import com.personalfinance.personalfinancetracker.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private String description;
}