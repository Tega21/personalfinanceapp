package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.DashboardSummary;
import com.personalfinance.personalfinancetracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserDetails userDetails) {

        LocalDate now = LocalDate.now();
        int resolvedMonth = (month != null) ? month : now.getMonthValue();
        int resolvedYear = (year != null) ? year : now.getYear();

        return ResponseEntity.ok(
                dashboardService.getDashboardSummary(
                        userDetails.getUsername(), resolvedMonth, resolvedYear)
        );
    }
}