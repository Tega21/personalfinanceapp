package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.CategoryRequest;
import com.personalfinance.personalfinancetracker.dto.CategoryResponse;
import com.personalfinance.personalfinancetracker.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                categoryService.createCategory(request, userDetails.getUsername())
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getUserCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                categoryService.getUserCategories(userDetails.getUsername())
        );
    }
}