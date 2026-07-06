package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.BudgetRequest;
import com.personalfinance.personalfinancetracker.dto.BudgetResponse;
import com.personalfinance.personalfinancetracker.entity.Budget;
import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.DuplicateResourceException;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.exception.UnauthorizedActionException;
import com.personalfinance.personalfinancetracker.repository.BudgetRepository;
import com.personalfinance.personalfinancetracker.repository.CategoryRepository;
import com.personalfinance.personalfinancetracker.repository.TransactionRepository;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    // Budget status values
    public static final String STATUS_OK = "OK";
    public static final String STATUS_WARNING = "WARNING";
    public static final String STATUS_EXCEEDED = "EXCEEDED";

    public BudgetResponse createBudget(BudgetRequest request, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to use this category");
        }

        if (budgetRepository.existsByCategory_IdAndUser_IdAndMonthAndYear(
                category.getId(), user.getId(), request.getMonth(), request.getYear())) {
            throw new DuplicateResourceException("A budget already exists for this category");
        }
        Budget budget = Budget.builder()
                .limitAmount(request.getAmountLimit())
                .month(request.getMonth())
                .year((request.getYear()))
                .category(category)
                .user(user)
                .build();

        Budget saved = budgetRepository.save(budget);
        return mapToResponse(saved);
    }

    public BudgetResponse mapToResponse(Budget budget) {
        LocalDate startDate = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal spent = transactionRepository.sumExpensesByUserAndCategoryAndDateRange(
                budget.getUser().getId(), budget.getCategory().getId(), startDate, endDate);

        BigDecimal percentUsed = spent
                .divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        String status;
        if (percentUsed.compareTo(BigDecimal.valueOf(100)) >= 0) {
            status = STATUS_EXCEEDED;
        } else if (percentUsed.compareTo(BigDecimal.valueOf(70)) >= 0) {
            status = STATUS_WARNING;
        } else {
            status = STATUS_OK;
        }

        return BudgetResponse.builder()
                .budgetId(budget.getId())
                .amountLimit(budget.getLimitAmount())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .month(budget.getMonth())
                .year(budget.getYear())
                .spent(spent)
                .percentUsed(percentUsed)
                .status(status)
                .build();
    }

    /**
     * Gets all budgets for the authenticated user for a given month
     * and year, with live spent/percentUsed/status calculated from actual
     * transactions at query time.
     *
     * @param username the authenticated user's username
     * @param month    the month to retrieve budgets for (1-12)
     * @param year     the year to retrieve budgets for
     * @return list of budgets with live spending calculations
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByMonth(String username, Integer month, Integer year) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return budgetRepository.findAllByUser_IdAndMonthAndYear(user.getId(), month, year)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates existing budget's amount limit after it makes sure to verify it belongs
     * to authenticated user.
     *
     * @param id       the ID of the budget to update
     * @param request  the updated budget details
     * @param username the authenticated user's username
     * @return the updated budget with recalculated spent/percentUsed/status
     * @throws ResourceNotFoundException   if the user or budget doesn't exist
     * @throws UnauthorizedActionException if the budget belongs to a different user
     */
    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to modify this budget");

        }

        budget.setLimitAmount(request.getAmountLimit());
        Budget saved = budgetRepository.save(budget);
        return mapToResponse(saved);
    }

    /**
     * Deletes a budget, after verifying it belongs to the authenticated user.
     *
     * @param id the ID of the budget to delete
     * @param username the authenticated user's username
     * @throws ResourceNotFoundException if the user or budget doesn't exist
     * @throws UnauthorizedActionException if the budget belongs to a different user
     */
    public void deleteBudget(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You do not have permission to modify this budget");
        }

        budgetRepository.delete(budget);

    }
}
