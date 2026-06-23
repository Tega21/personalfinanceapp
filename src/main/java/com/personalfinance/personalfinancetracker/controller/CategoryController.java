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
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Exposes category endpoints under /api/categories. Categories are
 * scope with each user. They get their own set and there are 15
 * default categories, and then any categories the user create
 * themselves. All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     *Creates new customer category for user
     *
     *  @param request the category's name, type, and description
     * @param userDetails authenticated user, injected from JWT
     * @return 200 OK with the created category
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                categoryService.createCategory(request, userDetails.getUsername())
        );
    }

    /**
     * Deletes a category, after verifying it belongs to the authenticated user.
     *
     * @param id the ID of the category to delete
     * @param userDetails the authenticated user, injected from the JWT
     * @return 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        categoryService.deleteCategory(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets all categories that belong to the authenticated user,
     * and that includes the default ones and any that they created
     * @param userDetails authenticated user injected from JWT
     * @return 200 OK with list of the User's categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getUserCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                categoryService.getUserCategories(userDetails.getUsername())
        );
    }
}