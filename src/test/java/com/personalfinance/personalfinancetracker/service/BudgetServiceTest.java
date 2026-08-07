package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.BudgetRequest;
import com.personalfinance.personalfinancetracker.dto.BudgetResponse;
import com.personalfinance.personalfinancetracker.entity.Budget;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.CategoryType;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.DuplicateResourceException;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.exception.UnauthorizedActionException;
import com.personalfinance.personalfinancetracker.repository.BudgetRepository;
import com.personalfinance.personalfinancetracker.repository.CategoryRepository;
import com.personalfinance.personalfinancetracker.repository.TransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Category testCategory;
    private BudgetRequest budgetRequest;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        testCategory = Category.builder()
                .id(4L)
                .name("Groceries")
                .categoryType(CategoryType.EXPENSE)
                .user(testUser)
                .build();

        budgetRequest = new BudgetRequest();
        budgetRequest.setCategoryId(4L);
        budgetRequest.setAmountLimit(new BigDecimal("200.00"));
        budgetRequest.setMonth(7);
        budgetRequest.setYear(2026);

        testBudget = Budget.builder()
                .id(1L)
                .limitAmount(new BigDecimal("200.00"))
                .month(7)
                .year(2026)
                .category(testCategory)
                .user(testUser)
                .build();
    }

    // Happy path: valid request with no existing budget for this
    // category/month/year should create and return a budget response
    // with correctly calculated spent/percentUsed/status.
    @Test
    void createBudget_withValidData_returnsBudgetResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(testCategory));
        when(budgetRepository.existsByCategory_IdAndUser_IdAndMonthAndYear(4L, 1L, 7, 2026))
                .thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        when(transactionRepository.sumExpensesByUserAndCategoryAndDateRange(
                eq(1L), eq(4L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.createBudget(budgetRequest, "testuser");

        assertNotNull(response);
        assertEquals(new BigDecimal("200.00"), response.getAmountLimit());
        assertEquals("Groceries", response.getCategoryName());
        assertEquals(BigDecimal.ZERO, response.getSpent());
        assertEquals(BudgetService.STATUS_OK, response.getStatus());
    }

    // A duplicate budget (same category + month + year) should be
    // rejected before anything gets saved.
    @Test
    void createBudget_withDuplicateBudget_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(testCategory));
        when(budgetRepository.existsByCategory_IdAndUser_IdAndMonthAndYear(4L, 1L, 7, 2026))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                budgetService.createBudget(budgetRequest, "testuser"));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // Trying to create a budget for a category that belongs to a
    // different user should be blocked.
    @Test
    void createBudget_withUnauthorizedCategory_throwsException() {
        User otherUser = User.builder().id(2L).username("other").email("other@example.com").password("pw").build();
        Category otherCategory = Category.builder()
                .id(4L).name("Groceries").categoryType(CategoryType.EXPENSE).user(otherUser).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(otherCategory));

        assertThrows(UnauthorizedActionException.class, () ->
                budgetService.createBudget(budgetRequest, "testuser"));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // mapToResponse should calculate spent/percentUsed/status correctly
    // from the transaction repository query result.
    @Test
    void mapToResponse_calculatesSpentAndStatus() {
        when(transactionRepository.sumExpensesByUserAndCategoryAndDateRange(
                eq(1L), eq(4L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("150.00"));

        BudgetResponse response = budgetService.mapToResponse(testBudget);

        assertEquals(new BigDecimal("150.00"), response.getSpent());
        assertEquals(BudgetService.STATUS_WARNING, response.getStatus());
    }

    // Happy path: should return all budgets for the user for the
    // given month/year with live spending calculations.
    @Test
    void getBudgetsByMonth_returnsListOfBudgets() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(budgetRepository.findAllByUser_IdAndMonthAndYear(1L, 7, 2026))
                .thenReturn(List.of(testBudget));
        when(transactionRepository.sumExpensesByUserAndCategoryAndDateRange(
                eq(1L), eq(4L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        List<BudgetResponse> result = budgetService.getBudgetsByMonth("testuser", 7, 2026);

        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).getCategoryName());
    }

    // A month with no budgets should return an empty list, not an error.
    @Test
    void getBudgetsByMonth_withNoBudgets_returnsEmptyList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(budgetRepository.findAllByUser_IdAndMonthAndYear(1L, 7, 2026))
                .thenReturn(List.of());

        List<BudgetResponse> result = budgetService.getBudgetsByMonth("testuser", 7, 2026);

        assertTrue(result.isEmpty());
    }

    // Happy path: updating a budget you own with a new limit should
    // succeed and return the updated response.
    @Test
    void updateBudget_withValidOwnership_updatesSuccessfully() {
        BudgetRequest updateRequest = new BudgetRequest();
        updateRequest.setAmountLimit(new BigDecimal("300.00"));
        updateRequest.setCategoryId(4L);
        updateRequest.setMonth(7);
        updateRequest.setYear(2026);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        when(transactionRepository.sumExpensesByUserAndCategoryAndDateRange(
                eq(1L), eq(4L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        BudgetResponse response = budgetService.updateBudget(1L, updateRequest, "testuser");

        assertNotNull(response);
        verify(budgetRepository).save(any(Budget.class));
    }

    // Trying to update a budget belonging to a different user should
    // be blocked and nothing saved.
    @Test
    void updateBudget_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("other").email("other@example.com").password("pw").build();
        Budget otherBudget = Budget.builder()
                .id(1L).limitAmount(new BigDecimal("200.00"))
                .month(7).year(2026)
                .category(testCategory).user(otherUser).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(otherBudget));

        assertThrows(UnauthorizedActionException.class, () ->
                budgetService.updateBudget(1L, budgetRequest, "testuser"));

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // Happy path: deleting a budget you own should succeed.
    @Test
    void deleteBudget_withValidOwnership_deletesSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        budgetService.deleteBudget(1L, "testuser");

        verify(budgetRepository).delete(testBudget);
    }

    // Trying to delete a budget belonging to a different user should
    // be blocked and nothing deleted.
    @Test
    void deleteBudget_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("other").email("other@example.com").password("pw").build();
        Budget otherBudget = Budget.builder()
                .id(1L).limitAmount(new BigDecimal("200.00"))
                .month(7).year(2026)
                .category(testCategory).user(otherUser).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(otherBudget));

        assertThrows(UnauthorizedActionException.class, () ->
                budgetService.deleteBudget(1L, "testuser"));

        verify(budgetRepository, never()).delete(any(Budget.class));
    }

    // Deleting a budget ID that doesn't exist should 404, not crash.
    @Test
    void deleteBudget_withNonExistentBudget_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                budgetService.deleteBudget(999L, "testuser"));
    }
}