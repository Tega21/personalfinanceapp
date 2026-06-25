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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    // Declared because CategoryService's constructor needs it, even though
    // none of the methods we're testing actually use it.
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Pet Care");
        categoryRequest.setType(CategoryType.EXPENSE);
        categoryRequest.setDescription("Vet visits and supplies");
    }

    // Happy path: a brand-new category name for this user should be created successfully.
    @Test
    void createCategory_withValidData_returnsCategoryResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByNameAndUserId("Pet Care", 1L)).thenReturn(false);

        Category savedCategory = Category.builder()
                .id(5L)
                .name("Pet Care")
                .categoryType(CategoryType.EXPENSE)
                .description("Vet visits and supplies")
                .user(testUser)
                .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(categoryRequest, "testuser");

        assertNotNull(response);
        assertEquals("Pet Care", response.getName());
        assertEquals(CategoryType.EXPENSE, response.getType());
    }

    // If the user already has a category with this exact name, creation should
    // be rejected, and nothing should actually get saved.
    @Test
    void createCategory_withDuplicateName_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByNameAndUserId("Pet Care", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            categoryService.createCategory(categoryRequest, "testuser");
        });

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // If the username on the JWT somehow doesn't match a real user, that should
    // fail cleanly rather than proceeding.
    @Test
    void createCategory_withNonExistentUser_throwsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.createCategory(categoryRequest, "ghost");
        });
    }

    // Happy path: a user with existing categories should get them all back.
    @Test
    void getUserCategories_returnsListOfCategories() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Category cat1 = Category.builder().id(1L).name("Groceries").categoryType(CategoryType.EXPENSE).user(testUser).build();
        Category cat2 = Category.builder().id(2L).name("Income").categoryType(CategoryType.INCOME).user(testUser).build();
        when(categoryRepository.findByUserId(1L)).thenReturn(List.of(cat1, cat2));

        List<CategoryResponse> result = categoryService.getUserCategories("testuser");

        assertEquals(2, result.size());
        assertEquals("Groceries", result.get(0).getName());
    }

    // Edge case: a user with zero categories should get back an empty list,
    // not an error.
    @Test
    void getUserCategories_withNoCategories_returnsEmptyList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        List<CategoryResponse> result = categoryService.getUserCategories("testuser");

        assertTrue(result.isEmpty());
    }

    // Happy path: deleting a category you actually own should succeed.
    @Test
    void deleteCategory_withValidOwnership_deletesSuccessfully() {
        Category category = Category.builder().id(10L).name("Pet Care").categoryType(CategoryType.EXPENSE).user(testUser).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(10L, "testuser");

        // Confirm the actual delete call happened on the repository.
        verify(categoryRepository).delete(category);
    }

    // Security check: trying to delete a category that belongs to a DIFFERENT
    // user should be blocked, and the category should NOT be deleted.
    @Test
    void deleteCategory_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("otheruser").email("other@example.com").password("pw").build();
        Category category = Category.builder().id(10L).name("Pet Care").categoryType(CategoryType.EXPENSE).user(otherUser).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        assertThrows(UnauthorizedActionException.class, () -> {
            categoryService.deleteCategory(10L, "testuser");
        });

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // Trying to delete a category ID that doesn't exist at all should 404,
    // not crash or silently do nothing.
    @Test
    void deleteCategory_withNonExistentCategory_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory(999L, "testuser");
        });
    }

    // Checks the actual seeding logic: exactly 15 categories should be created,
    // with exactly one of them typed as INCOME (the rest EXPENSE).
    @Test
    void seedDefaultCategories_callsSaveAll() {
        categoryService.seedDefaultCategories(testUser);

        verify(categoryRepository).saveAll(anyList());
    }
}