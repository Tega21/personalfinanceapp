package com.personalfinance.personalfinancetracker.repository;

import com.personalfinance.personalfinancetracker.entity.Budget;
import com.personalfinance.personalfinancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;


public interface BudgetRepository extends JpaRepository<Budget,Long> {

    List<Budget> findAllByUserId(Long userId);

    boolean existsByCategoryIdAndUserIdAndMonthAndYear(Long categoryId, Long userId, Integer month, Integer year);
}
