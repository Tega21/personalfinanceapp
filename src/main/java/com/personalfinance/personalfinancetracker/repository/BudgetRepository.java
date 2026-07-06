package com.personalfinance.personalfinancetracker.repository;

import com.personalfinance.personalfinancetracker.entity.Budget;
import com.personalfinance.personalfinancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/**
 * Repository for Budget entities. Defined in the schema ahead of Sprint 2,
 * when the controller/service and API endpoints will be built.
 */

public interface BudgetRepository extends JpaRepository<Budget,Long> {

    List<Budget> findAllByUser_IdAndMonthAndYear(Long userId, Integer month, Integer year);

    boolean existsByCategory_IdAndUser_IdAndMonthAndYear(Long categoryId, Long userId, Integer month, Integer year);
}
