package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.BudgetRequest;
import com.personalfinance.personalfinancetracker.dto.BudgetResponse;
import com.personalfinance.personalfinancetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                budgetService.createBudget(request, userDetails.getUsername())
        );
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgetsByMonth(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                budgetService.getBudgetsByMonth(userDetails.getUsername(), month, year)
        );
    }

    /**
     * Updates an existing budget's amount limit.
     *
     * @param id the ID of the budget to update
     * @param request the updated budget details
     * @param userDetails the authenticated user, injected from the JWT
     * @return 200 OK with the updated budget
     */
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                budgetService.updateBudget(id, request, userDetails.getUsername())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        budgetService.deleteBudget(id,userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
