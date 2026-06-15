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

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.createTransaction(request, userDetails.getUsername())
        );
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.getUserTransactions(userDetails.getUsername())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        transactionService.deleteTransaction(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}