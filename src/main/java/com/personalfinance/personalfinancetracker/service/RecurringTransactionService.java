package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.RecurringTransactionRequest;
import com.personalfinance.personalfinancetracker.dto.RecurringTransactionResponse;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.RecurringTransaction;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.exception.UnauthorizedActionException;
import com.personalfinance.personalfinancetracker.repository.CategoryRepository;
import com.personalfinance.personalfinancetracker.repository.RecurringTransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for recurring transaction template management.
 * Handles all business logic for creating, retrieving, updating,
 * and deleting recurring transaction templates. All operations are
 * scoped to the authenticated user to enforce data isolation.
 */
@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Creates a new recurring transaction template for the authenticated user.
     *
     * @param username the username of the authenticated user
     * @param request  the recurring transaction details from the request body
     * @return the created template as a RecurringTransactionResponse DTO
     */
    @Transactional
    public RecurringTransactionResponse createRecurringTransaction(
            String username, RecurringTransactionRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(user);
        recurring.setCategory(category);
        recurring.setDescription(request.getDescription());
        recurring.setAmount(request.getAmount());
        recurring.setType(request.getType());
        recurring.setDayOfMonth(request.getDayOfMonth());

        return mapToResponse(recurringTransactionRepository.save(recurring));
    }

    /**
     * Retrieves all recurring transaction templates for the authenticated user.
     *
     * @param username the username of the authenticated user
     * @return list of RecurringTransactionResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getUserRecurringTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return recurringTransactionRepository.findByUser_Id(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing recurring transaction template.
     * Only the owner may update their template.
     *
     * @param username the username of the authenticated user
     * @param id       the ID of the template to update
     * @param request  the updated template details
     * @return the updated template as a RecurringTransactionResponse DTO
     */
    @Transactional
    public RecurringTransactionResponse updateRecurringTransaction(
            String username, Long id, RecurringTransactionRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RecurringTransaction recurring = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (!recurring.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException(
                    "You do not have permission to update this recurring transaction");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        recurring.setCategory(category);
        recurring.setDescription(request.getDescription());
        recurring.setAmount(request.getAmount());
        recurring.setType(request.getType());
        recurring.setDayOfMonth(request.getDayOfMonth());

        return mapToResponse(recurringTransactionRepository.save(recurring));
    }

    /**
     * Deletes a recurring transaction template.
     * Only the owner may delete their template.
     *
     * @param username the username of the authenticated user
     * @param id       the ID of the template to delete
     */
    @Transactional
    public void deleteRecurringTransaction(String username, Long id) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RecurringTransaction recurring = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (!recurring.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException(
                    "You do not have permission to delete this recurring transaction");
        }

        recurringTransactionRepository.delete(recurring);
    }

    /**
     * Maps a RecurringTransaction entity to a RecurringTransactionResponse DTO.
     *
     * @param recurring the RecurringTransaction entity
     * @return the corresponding RecurringTransactionResponse DTO
     */
    private RecurringTransactionResponse mapToResponse(RecurringTransaction recurring) {
        return new RecurringTransactionResponse(
                recurring.getId(),
                recurring.getDescription(),
                recurring.getAmount(),
                recurring.getType(),
                recurring.getCategory().getId(),
                recurring.getCategory().getName(),
                recurring.getDayOfMonth()
        );
    }
}