package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.CategoryRequest;
import com.personalfinance.personalfinancetracker.dto.CategoryResponse;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.CategoryType;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.DuplicateResourceException;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.exception.UnauthorizedActionException;
import com.personalfinance.personalfinancetracker.repository.CategoryRepository;
import com.personalfinance.personalfinancetracker.repository.TransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private record DefaultCategory(String name, CategoryType type) {}

    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            new DefaultCategory("Food & Dining", CategoryType.EXPENSE),
            new DefaultCategory("Groceries", CategoryType.EXPENSE),
            new DefaultCategory("Transportation", CategoryType.EXPENSE),
            new DefaultCategory("Housing", CategoryType.EXPENSE),
            new DefaultCategory("Utilities", CategoryType.EXPENSE),
            new DefaultCategory("Bills & Subscriptions", CategoryType.EXPENSE),
            new DefaultCategory("Shopping", CategoryType.EXPENSE),
            new DefaultCategory("Entertainment", CategoryType.EXPENSE),
            new DefaultCategory("Health & Medical", CategoryType.EXPENSE),
            new DefaultCategory("Personal Care", CategoryType.EXPENSE),
            new DefaultCategory("Travel", CategoryType.EXPENSE),
            new DefaultCategory("Education", CategoryType.EXPENSE),
            new DefaultCategory("Gifts & Donations", CategoryType.EXPENSE),
            new DefaultCategory("Income", CategoryType.INCOME),
            new DefaultCategory("Other", CategoryType.EXPENSE)
    );
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryResponse createCategory(CategoryRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new DuplicateResourceException("Category already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .categoryType(request.getType())
                .description(request.getDescription())
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);

    }

    public List<CategoryResponse> getUserCategories(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a category, after verifying it belongs to the authenticated user.
     *
     * @param id the ID of the category to delete
     * @param username the authenticated user's username
     */
    public void deleteCategory(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to delete this category");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category){
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getCategoryType())
                .description(category.getDescription())
                .build();
    }

    /**
     * Creates the 15 default categories for a newly registered user.
     * Called once at registration so every user starts with a usable
     * category list without having to create categories manually.
     *
     * @param user the newly registered user to seed categories for
     */
    public void seedDefaultCategories(User user) {
        List<Category> categories = DEFAULT_CATEGORIES.stream()
                .map(defaultCategory -> Category.builder()
                        .name(defaultCategory.name())
                        .categoryType(defaultCategory.type())
                        .user(user)
                        .build())
                .collect(Collectors.toList());

        categoryRepository.saveAll(categories);
    }
}
