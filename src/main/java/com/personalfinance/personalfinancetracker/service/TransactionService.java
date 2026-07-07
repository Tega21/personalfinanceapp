package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.TransactionRequest;
import com.personalfinance.personalfinancetracker.dto.TransactionResponse;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.Transaction;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.exception.UnauthorizedActionException;
import com.personalfinance.personalfinancetracker.repository.CategoryRepository;
import com.personalfinance.personalfinancetracker.repository.TransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages transaction creation, retrieval, update, and deletion,
 * the core CRUD operations behind the Transactions screen and the
 * data source for the Dashboard's aggregated totals.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new transaction for the authenticated user.
     *
     * @param request the transaction's amount, type, date, description, and category
     * @param username the authenticated user's username
     * @return the created transaction
     * @throws ResourceNotFoundException if the user or category doesn't exist
     */
    public TransactionResponse createTransaction(TransactionRequest request, String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .category(category)
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);

    }

    /**
     * Retrieves transactions for the authenticated user with optional
     * filters for category, date range, and keyword search.
     *
     * @param username the authenticated user's username
     * @param categoryId optional category ID filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param keyword optional description keyword filter
     * @return filtered transactions sorted by most recent date first
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(
            String username,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            String keyword) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId())
                .stream()
                .filter(t -> categoryId == null || t.getCategory().getId().equals(categoryId))
                .filter(t -> startDate == null || !t.getTransactionDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getTransactionDate().isAfter(endDate))
                .filter(t -> keyword == null || t.getDescription() != null &&
                        t.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a transaction, after verifying it belongs to the
     * authenticated user.
     *
     * @param id the ID of the transaction to delete
     * @param username the authenticated user's username
     * @throws ResourceNotFoundException if the user or transaction doesn't exist
     * @throws UnauthorizedActionException if the transaction belongs to a different user
     */
    public void deleteTransaction(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to delete this transaction");
        }

        transactionRepository.delete(transaction);
    }

    /**
     * Updates an existing transaction, after verifying it belongs to
     * the authenticated user.
     *
     * @param id the ID of the transaction to update
     * @param request the updated amount, type, date, description, and category
     * @param username the authenticated user's username
     * @return the updated transaction
     * @throws ResourceNotFoundException if the user, transaction, or category doesn't exist
     * @throws UnauthorizedActionException if the transaction belongs to a different user
     */
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to modify this transaction");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Converts a Transaction entity into its response DTO, flattening
     * the category relationship into categoryName/categoryId.
     *
     * @param transaction the entity to convert
     * @return the corresponding response DTO
     */
    public TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .categoryName(transaction.getCategory().getName())
                .categoryId(transaction.getCategory().getId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }


}
