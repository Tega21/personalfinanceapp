package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.RecurringTransactionRequest;
import com.personalfinance.personalfinancetracker.dto.RecurringTransactionResponse;
import com.personalfinance.personalfinancetracker.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for recurring transaction template management.
 * All endpoints require a valid JWT. User identity is resolved
 * from the authenticated principal.
 */
@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    /**
     * Creates a new recurring transaction template for the authenticated user.
     *
     * @param userDetails the authenticated user from the JWT
     * @param request     the recurring transaction details
     * @return 201 Created with the new RecurringTransactionResponse
     */
    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> createRecurringTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RecurringTransactionRequest request) {

        RecurringTransactionResponse response = recurringTransactionService
                .createRecurringTransaction(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all recurring transaction templates for the authenticated user.
     *
     * @param userDetails the authenticated user from the JWT
     * @return 200 OK with list of RecurringTransactionResponse DTOs
     */
    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> getUserRecurringTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                recurringTransactionService.getUserRecurringTransactions(
                        userDetails.getUsername()));
    }

    /**
     * Updates an existing recurring transaction template.
     * Only the owner may update.
     *
     * @param userDetails the authenticated user from the JWT
     * @param id          the ID of the template to update
     * @param request     the updated template details
     * @return 200 OK with the updated RecurringTransactionResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> updateRecurringTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RecurringTransactionRequest request) {

        RecurringTransactionResponse response = recurringTransactionService
                .updateRecurringTransaction(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a recurring transaction template.
     * Only the owner may delete.
     *
     * @param userDetails the authenticated user from the JWT
     * @param id          the ID of the template to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        recurringTransactionService.deleteRecurringTransaction(
                userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}