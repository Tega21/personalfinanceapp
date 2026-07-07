package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.TransactionRequest;
import com.personalfinance.personalfinancetracker.dto.TransactionResponse;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.CategoryType;
import com.personalfinance.personalfinancetracker.entity.Transaction;
import com.personalfinance.personalfinancetracker.entity.TransactionType;
import com.personalfinance.personalfinancetracker.entity.User;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Category testCategory;
    private TransactionRequest transactionRequest;

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

        transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(new BigDecimal("75.50"));
        transactionRequest.setType(TransactionType.EXPENSE);
        transactionRequest.setTransactionDate(LocalDate.of(2026, 6, 15));
        transactionRequest.setCategoryId(4L);
        transactionRequest.setDescription("Groceries");
    }

    // Happy path: valid amount, type, date, and an existing category should
    // create successfully and come back mapped into a response.
    @Test
    void createTransaction_withValidData_returnsTransactionResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(testCategory));

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 15))
                .description("Groceries")
                .category(testCategory)
                .user(testUser)
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.createTransaction(transactionRequest, "testuser");

        assertNotNull(response);
        assertEquals(new BigDecimal("75.50"), response.getAmount());
        assertEquals("Groceries", response.getCategoryName());
        assertEquals(TransactionType.EXPENSE, response.getType());
    }

    // If the categoryId on the request doesn't exist, creation should fail
    // cleanly rather than saving a transaction with a broken category link.
    @Test
    void createTransaction_withNonExistentCategory_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(transactionRequest, "testuser");
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // Happy path: a user with existing transactions should get them all back.
    @Test
    void getUserTransactions_returnsListOfTransactions() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 15))
                .description("Groceries")
                .category(testCategory)
                .user(testUser)
                .build();
        when(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> result = transactionService.getUserTransactions(
                "testuser", null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).getCategoryName());
    }

    // Edge case: a brand-new user with zero transactions should get an empty
    // list back, not an error.
    @Test
    void getUserTransactions_withNoTransactions_returnsEmptyList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                .thenReturn(Collections.emptyList());

        List<TransactionResponse> result = transactionService.getUserTransactions(
                "testuser", null, null, null, null);

        assertTrue(result.isEmpty());
    }

    // Happy path: updating a transaction you own should succeed and reflect
    // the new values.
    @Test
    void updateTransaction_withValidOwnership_updatesSuccessfully() {
        Transaction existingTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 10))
                .description("Old description")
                .category(testCategory)
                .user(testUser)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        TransactionResponse response = transactionService.updateTransaction(1L, transactionRequest, "testuser");

        assertNotNull(response);
        // Confirm the in-memory object was actually mutated with the new values.
        assertEquals(new BigDecimal("75.50"), existingTransaction.getAmount());
        assertEquals("Groceries", existingTransaction.getDescription());
    }

    // Security check: trying to update a transaction belonging to a DIFFERENT
    // user should be blocked, and nothing should actually get saved.
    @Test
    void updateTransaction_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("otheruser").email("other@example.com").password("pw").build();
        Transaction existingTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 10))
                .category(testCategory)
                .user(otherUser)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));

        assertThrows(UnauthorizedActionException.class, () -> {
            transactionService.updateTransaction(1L, transactionRequest, "testuser");
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // Updating an id that doesn't exist at all should 404, not crash.
    @Test
    void updateTransaction_withNonExistentTransaction_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.updateTransaction(999L, transactionRequest, "testuser");
        });
    }

    // Happy path: deleting a transaction you own should succeed.
    @Test
    void deleteTransaction_withValidOwnership_deletesSuccessfully() {
        Transaction existingTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 15))
                .category(testCategory)
                .user(testUser)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));

        transactionService.deleteTransaction(1L, "testuser");

        verify(transactionRepository).delete(existingTransaction);
    }

    // Security check: deleting another user's transaction should be blocked.
    @Test
    void deleteTransaction_withWrongOwner_throwsException() {
        User otherUser = User.builder().id(2L).username("otheruser").email("other@example.com").password("pw").build();
        Transaction existingTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 15))
                .category(testCategory)
                .user(otherUser)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));

        assertThrows(UnauthorizedActionException.class, () -> {
            transactionService.deleteTransaction(1L, "testuser");
        });

        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    // Deleting an id that doesn't exist at all should 404, not crash.
    @Test
    void deleteTransaction_withNonExistentTransaction_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.deleteTransaction(999L, "testuser");
        });
    }

    // Checks the field-by-field mapping logic directly: every field on the
    // entity should land in the correct field on the response DTO.
    @Test
    void mapToResponse_mapsAllFieldsCorrectly() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 6, 15))
                .description("Groceries")
                .category(testCategory)
                .user(testUser)
                .build();

        TransactionResponse response = transactionService.mapToResponse(transaction);

        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("75.50"), response.getAmount());
        assertEquals(TransactionType.EXPENSE, response.getType());
        assertEquals(LocalDate.of(2026, 6, 15), response.getTransactionDate());
        assertEquals("Groceries", response.getDescription());
        assertEquals("Groceries", response.getCategoryName());
        assertEquals(4L, response.getCategoryId());
    }
}