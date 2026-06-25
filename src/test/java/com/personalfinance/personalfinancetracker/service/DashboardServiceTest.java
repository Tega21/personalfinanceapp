package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.CategoryBreakdown;
import com.personalfinance.personalfinancetracker.dto.DashboardSummary;
import com.personalfinance.personalfinancetracker.dto.TransactionResponse;
import com.personalfinance.personalfinancetracker.entity.Transaction;
import com.personalfinance.personalfinancetracker.entity.TransactionType;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
    }

    // Happy path: a month with real income and expense data should come back
    // with correct totals, a correctly calculated net cash flow, a category
    // breakdown, and recent transactions mapped through TransactionService.
    @Test
    void getDashboardSummary_withTransactions_returnsCorrectTotals() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(transactionRepository.sumByUserIdAndTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("2000.00"));

        when(transactionRepository.sumByUserIdAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("75.50"));

        CategoryBreakdown groceriesBreakdown = CategoryBreakdown.builder()
                .categoryId(4L)
                .categoryName("Groceries")
                .total(new BigDecimal("75.50"))
                .build();
        when(transactionRepository.findCategoryBreakdown(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(groceriesBreakdown));

        Transaction groceryTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 15))
                .description("Groceries")
                .user(testUser)
                .build();
        when(transactionRepository.findTop5ByUserIdOrderByTransactionDateDesc(1L))
                .thenReturn(List.of(groceryTransaction));

        TransactionResponse mappedResponse = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 15))
                .description("Groceries")
                .categoryName("Groceries")
                .categoryId(4L)
                .build();
        when(transactionService.mapToResponse(groceryTransaction)).thenReturn(mappedResponse);

        // Act
        DashboardSummary summary = dashboardService.getDashboardSummary("testuser", 6, 2026);

        // Assert
        assertEquals(new BigDecimal("2000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("75.50"), summary.getTotalExpenses());
        // The real money check: net cash flow must equal income minus expenses.
        assertEquals(new BigDecimal("1924.50"), summary.getNetCashFlow());
        assertEquals(1, summary.getCategoryBreakdown().size());
        assertEquals("Groceries", summary.getCategoryBreakdown().get(0).getCategoryName());
        assertEquals(1, summary.getRecentTransactions().size());
        assertEquals("Groceries", summary.getRecentTransactions().get(0).getDescription());
    }

    // Edge case: a month with zero transactions should come back with zeroed
    // totals and empty lists, not an error or a null-pointer crash. This is
    // the case the COALESCE(SUM(...), 0) fix in the repository query protects.
    @Test
    void getDashboardSummary_withNoTransactions_returnsZeroedTotals() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(transactionRepository.sumByUserIdAndTypeAndDateRange(
                anyLong(), any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        when(transactionRepository.findCategoryBreakdown(
                anyLong(), any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(transactionRepository.findTop5ByUserIdOrderByTransactionDateDesc(1L))
                .thenReturn(Collections.emptyList());

        DashboardSummary summary = dashboardService.getDashboardSummary("testuser", 1, 2020);

        assertEquals(BigDecimal.ZERO, summary.getTotalIncome());
        assertEquals(BigDecimal.ZERO, summary.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, summary.getNetCashFlow());
        assertTrue(summary.getCategoryBreakdown().isEmpty());
        assertTrue(summary.getRecentTransactions().isEmpty());
    }

    // If the username doesn't correspond to a real user, this should fail
    // cleanly rather than proceeding with a null user.
    @Test
    void getDashboardSummary_withNonExistentUser_throwsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            dashboardService.getDashboardSummary("ghost", 6, 2026);
        });
    }


}