package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.AccountRequest;
import com.personalfinance.personalfinancetracker.dto.AccountResponse;
import com.personalfinance.personalfinancetracker.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for financial account management.
 * All endpoints require a valid JWT. User identity is resolved
 * from the authenticated principal.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Creates a new account for the authenticated user.
     *
     * @param userDetails the authenticated user from the JWT
     * @param request     the account details
     * @return 201 Created with the new AccountResponse
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AccountRequest request) {

        AccountResponse response = accountService.createAccount(
                userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all accounts for the authenticated user.
     *
     * @param userDetails the authenticated user from the JWT
     * @return 200 OK with list of AccountResponse DTOs
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                accountService.getUserAccounts(userDetails.getUsername()));
    }

    /**
     * Updates an existing account. Only the owner may update.
     *
     * @param userDetails the authenticated user from the JWT
     * @param accountId   the ID of the account to update
     * @param request     the updated account details
     * @return 200 OK with the updated AccountResponse
     */
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @Valid @RequestBody AccountRequest request) {

        AccountResponse response = accountService.updateAccount(
                userDetails.getUsername(), accountId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an account. Only the owner may delete.
     *
     * @param userDetails the authenticated user from the JWT
     * @param accountId   the ID of the account to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId) {

        accountService.deleteAccount(userDetails.getUsername(), accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the total net worth for the authenticated user,
     * calculated by summing balances across all accounts.
     *
     * @param userDetails the authenticated user from the JWT
     * @return 200 OK with net worth as a BigDecimal
     */
    @GetMapping("/net-worth")
    public ResponseEntity<BigDecimal> getNetWorth(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                accountService.getNetWorth(userDetails.getUsername()));
    }
}