package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.RecurringTransactionRequest;
import com.personalfinance.personalfinancetracker.dto.RecurringTransactionResponse;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.CategoryType;
import com.personalfinance.personalfinancetracker.entity.RecurringTransaction;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.exception.UnauthorizedActionException;
import com.personalfinance.personalfinancetracker.repository.CategoryRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    private User testUser;
    private Category testCategory;
    private RecurringTransactionRequest recurringRequest;
    private RecurringTransaction testRecurring;

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

        recurringRequest = new RecurringTransactionRequest();
        recurringRequest.setDescription("Netflix");
        recurringRequest.setAmount(new BigDecimal("15.99"));
        recurringRequest.setType(RecurringTransaction.TransactionType.EXPENSE);
        recurringRequest.setCategoryId(4L);
        recurringRequest.setDayOfMonth(15);

        testRecurring = new RecurringTransaction();
        testRecurring.setId(1L);
        testRecurring.setUser(testUser);
        testRecurring.setCategory(testCategory);
        testRecurring.setDescription("Netflix");
        testRecurring.setAmount(new BigDecimal("15.99"));
        testRecurring.setType(RecurringTransaction.TransactionType.EXPENSE);
        testRecurring.setDayOfMonth(15);
    }

    // Happy path: a valid request should create and return a recurring
    // transaction response with the correct fields.
    @Test
    void createRecurringTransaction_withValidData_returnsResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(testCategory));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class)))
                .thenReturn(testRecurring);

        RecurringTransactionResponse response =
                recurringTransactionService.createRecurringTransaction("testuser", recurringRequest);

        assertNotNull(response);
        assertEquals("Netflix", response.getDescription());
        assertEquals(new BigDecimal("15.99"), response.getAmount());
        assertEquals(RecurringTransaction.TransactionType.EXPENSE, response.getType());
        assertEquals("Groceries", response.getCategoryName());
        assertEquals(15, response.getDayOfMonth());
    }

    // Creating with a username that doesn't exist should 404.
    @Test
    void createRecurringTransaction_withNonExistentUser_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                recurringTransactionService.createRecurringTransaction("testuser", recurringRequest));

        verify(recurringTransactionRepository, never()).save(any(RecurringTransaction.class));
    }

    // Creating with a category that doesn't exist should 404.
    @Test
    void createRecurringTransaction_withNonExistentCategory_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                recurringTransactionService.createRecurringTransaction("testuser", recurringRequest));

        verify(recurringTransactionRepository, never()).save(any(RecurringTransaction.class));
    }

    // Happy path: should return all recurring transactions for the user.
    @Test
    void getUserRecurringTransactions_returnsList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser_Id(1L)).thenReturn(List.of(testRecurring));

        List<RecurringTransactionResponse> result =
                recurringTransactionService.getUserRecurringTransactions("testuser");

        assertEquals(1, result.size());
        assertEquals("Netflix", result.get(0).getDescription());
    }

    // A user with no recurring transactions should get an empty list.
    @Test
    void getUserRecurringTransactions_withNone_returnsEmptyList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser_Id(1L)).thenReturn(List.of());

        List<RecurringTransactionResponse> result =
                recurringTransactionService.getUserRecurringTransactions("testuser");

        assertTrue(result.isEmpty());
    }

    // Happy path: updating a recurring transaction you own should
    // succeed and return the updated response.
    @Test
    void updateRecurringTransaction_withValidOwnership_updatesSuccessfully() {
        RecurringTransactionRequest updateRequest = new RecurringTransactionRequest();
        updateRequest.setDescription("Netflix Premium");
        updateRequest.setAmount(new BigDecimal("22.99"));
        updateRequest.setType(RecurringTransaction.TransactionType.EXPENSE);
        updateRequest.setCategoryId(4L);
        updateRequest.setDayOfMonth(15);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findById(1L)).thenReturn(Optional.of(testRecurring));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(testCategory));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class)))
                .thenReturn(testRecurring);

        RecurringTransactionResponse response =
                recurringTransactionService.updateRecurringTransaction("testuser", 1L, updateRequest);

        assertNotNull(response);
        verify(recurringTransactionRepository).save(any(RecurringTransaction.class));
    }

    // Trying to update a recurring transaction belonging to another
    // user should be blocked and nothing saved.
    @Test
    void updateRecurringTransaction_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("other").email("other@example.com").password("pw").build();
        RecurringTransaction otherRecurring = new RecurringTransaction();
        otherRecurring.setId(1L);
        otherRecurring.setUser(otherUser);
        otherRecurring.setCategory(testCategory);
        otherRecurring.setDescription("Someone else's");
        otherRecurring.setAmount(new BigDecimal("10.00"));
        otherRecurring.setType(RecurringTransaction.TransactionType.EXPENSE);
        otherRecurring.setDayOfMonth(5);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findById(1L)).thenReturn(Optional.of(otherRecurring));

        assertThrows(UnauthorizedActionException.class, () ->
                recurringTransactionService.updateRecurringTransaction("testuser", 1L, recurringRequest));

        verify(recurringTransactionRepository, never()).save(any(RecurringTransaction.class));
    }

    // Updating an ID that doesn't exist should 404.
    @Test
    void updateRecurringTransaction_withNonExistent_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                recurringTransactionService.updateRecurringTransaction("testuser", 999L, recurringRequest));

        verify(recurringTransactionRepository, never()).save(any(RecurringTransaction.class));
    }

    // Happy path: deleting a recurring transaction you own should succeed.
    @Test
    void deleteRecurringTransaction_withValidOwnership_deletesSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findById(1L)).thenReturn(Optional.of(testRecurring));

        recurringTransactionService.deleteRecurringTransaction("testuser", 1L);

        verify(recurringTransactionRepository).delete(testRecurring);
    }

    // Trying to delete a recurring transaction belonging to another
    // user should be blocked and nothing deleted.
    @Test
    void deleteRecurringTransaction_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("other").email("other@example.com").password("pw").build();
        RecurringTransaction otherRecurring = new RecurringTransaction();
        otherRecurring.setId(1L);
        otherRecurring.setUser(otherUser);
        otherRecurring.setCategory(testCategory);
        otherRecurring.setDescription("Someone else's");
        otherRecurring.setAmount(new BigDecimal("10.00"));
        otherRecurring.setType(RecurringTransaction.TransactionType.EXPENSE);
        otherRecurring.setDayOfMonth(5);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findById(1L)).thenReturn(Optional.of(otherRecurring));

        assertThrows(UnauthorizedActionException.class, () ->
                recurringTransactionService.deleteRecurringTransaction("testuser", 1L));

        verify(recurringTransactionRepository, never()).delete(any(RecurringTransaction.class));
    }

    // Deleting an ID that doesn't exist should 404.
    @Test
    void deleteRecurringTransaction_withNonExistent_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                recurringTransactionService.deleteRecurringTransaction("testuser", 999L));

        verify(recurringTransactionRepository, never()).delete(any(RecurringTransaction.class));
    }
}