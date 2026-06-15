package com.personalfinance.personalfinancetracker.repository;

import com.personalfinance.personalfinancetracker.entity.Category;
import com.personalfinance.personalfinancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
}