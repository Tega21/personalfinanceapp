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

/**
 * Exposes dashboard endpoint under /api/dahsboard.
 * Gives financial overview that will power the main
 * dashboard screen.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Gets the dashboard summart fo authenticated user for
     * the given month/year. If month or year is excluded, it
     * defaults to current month/year, so that way the frontend
     * can call this with no parameters to show "this month"
     * by default.
     *
     * @param month the month(1-12); will default to current if omitted
     * @param year the year to summarize; will default to current if omitted
     * @param userDetails authenticated user, injected from JWT
     * @return 200 OK with the dashboard summary for the requested month/year
     */
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