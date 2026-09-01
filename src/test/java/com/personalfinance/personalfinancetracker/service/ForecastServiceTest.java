package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.ForecastResponse;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.CategoryType;
import com.personalfinance.personalfinancetracker.entity.RecurringTransaction;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.repository.RecurringTransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ForecastService forecastService;

    private User testUser;
    private Category testCategory;
    private RecurringTransaction expenseRecurring;
    private RecurringTransaction incomeRecurring;

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

        expenseRecurring = new RecurringTransaction();
        expenseRecurring.setId(1L);
        expenseRecurring.setUser(testUser);
        expenseRecurring.setCategory(testCategory);
        expenseRecurring.setDescription("Netflix");
        expenseRecurring.setAmount(new BigDecimal("15.99"));
        expenseRecurring.setType(RecurringTransaction.TransactionType.EXPENSE);
        expenseRecurring.setDayOfMonth(15);

        incomeRecurring = new RecurringTransaction();
        incomeRecurring.setId(2L);
        incomeRecurring.setUser(testUser);
        incomeRecurring.setCategory(testCategory);
        incomeRecurring.setDescription("Paycheck");
        incomeRecurring.setAmount(new BigDecimal("2000.00"));
        incomeRecurring.setType(RecurringTransaction.TransactionType.INCOME);
        incomeRecurring.setDayOfMonth(1);
    }

    // Happy path: a 30-day forecast with recurring transactions should
    // return the correct window size and non-null projected totals.
    @Test
    void getForecast_with30Days_returnsProjectedTotals() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser_Id(1L))
                .thenReturn(List.of(expenseRecurring, incomeRecurring));

        ForecastResponse response = forecastService.getForecast("testuser", 30);

        assertNotNull(response);
        assertEquals(30, response.getDays());
        assertNotNull(response.getProjectedIncome());
        assertNotNull(response.getProjectedExpenses());
        assertNotNull(response.getProjectedNet());
    }

    // With no recurring transactions, all projected totals should be zero.
    @Test
    void getForecast_withNoRecurring_returnsZeros() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser_Id(1L)).thenReturn(List.of());

        ForecastResponse response = forecastService.getForecast("testuser", 60);

        assertEquals(0, BigDecimal.ZERO.compareTo(response.getProjectedIncome()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getProjectedExpenses()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getProjectedNet()));
        assertTrue(response.getUpcomingTransactions().isEmpty());
    }

    // Net should equal projected income minus projected expenses.
    @Test
    void getForecast_netEqualsIncomeMinusExpenses() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser_Id(1L))
                .thenReturn(List.of(expenseRecurring, incomeRecurring));

        ForecastResponse response = forecastService.getForecast("testuser", 90);

        BigDecimal expectedNet = response.getProjectedIncome()
                .subtract(response.getProjectedExpenses());
        assertEquals(0, expectedNet.compareTo(response.getProjectedNet()));
    }

    // A longer window should project at least as much expense as a
    // shorter one (more occurrences of the same recurring item).
    @Test
    void getForecast_longerWindowProjectsMoreOrEqual() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser_Id(1L))
                .thenReturn(List.of(expenseRecurring, incomeRecurring));

        ForecastResponse thirty = forecastService.getForecast("testuser", 30);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser_Id(1L))
                .thenReturn(List.of(expenseRecurring, incomeRecurring));

        ForecastResponse ninety = forecastService.getForecast("testuser", 90);

        assertTrue(ninety.getProjectedExpenses().compareTo(thirty.getProjectedExpenses()) >= 0);
    }

    // A username that doesn't exist should 404.
    @Test
    void getForecast_withNonExistentUser_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                forecastService.getForecast("testuser", 30));
    }
}