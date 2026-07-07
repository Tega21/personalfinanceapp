package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.TransactionRequest;
import com.personalfinance.personalfinancetracker.dto.TransactionResponse;
import com.personalfinance.personalfinancetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Exposes transactions REST endpoints under /api/transactions.
 * Handles CRUD operations on a user's transactions. All endpoints
 * need authentication and only operate on that user's own data.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates a new transaction for the authenticated user
     *
     * @param request the transaction amount, type, date, description, and category
     * @param userDetails authenticated user injected from JWT
     * @return 200 OK with the created transcation
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                transactionService.createTransaction(request, userDetails.getUsername())
        );
    }

    /**
     * Retrieves the authenticated user's transactions with optional
     * filters for category, date range, and keyword search on description.
     *
     * @param categoryId optional category ID filter
     * @param startDate optional start date filter (yyyy-MM-dd)
     * @param endDate optional end date filter (yyyy-MM-dd)
     * @param keyword optional description keyword filter
     * @param userDetails the authenticated user, injected from the JWT
     * @return 200 OK with the filtered transaction list
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.getUserTransactions(
                        userDetails.getUsername(), categoryId, startDate, endDate, keyword)
        );
    }

    /**
     * Deletes a transaction after verifying it belongs to the user.
     *
     * @param id ID of transaction to delete
     * @param userDetails authenticated user injected from JWT
     * @return 204 No Content on a successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        transactionService.deleteTransaction(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates existing transaction, after verifying it belongs to user.
     * @param id ID of transaction to update
     * @param request the updated amount, type, date, description, and category
     * @param userDetails authenticated user injected from JWT
     * @return 200 OK with updated transaction
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.updateTransaction(id, request, userDetails.getUsername())
        );
    }
}