package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.ForecastResponse;
import com.personalfinance.personalfinancetracker.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for cash flow forecasting.
 * All endpoints require a valid JWT. User identity is resolved
 * from the authenticated principal.
 */
@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    /**
     * Returns a cash flow forecast for the authenticated user.
     * Projects recurring transactions forward across the specified
     * number of days and returns projected income, expenses, and net balance.
     *
     * @param userDetails the authenticated user from the JWT
     * @param days        number of days to forecast — 30, 60, or 90 (defaults to 30)
     * @return 200 OK with ForecastResponse containing projected totals
     */
    @GetMapping
    public ResponseEntity<ForecastResponse> getForecast(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "30") int days) {

        return ResponseEntity.ok(
                forecastService.getForecast(userDetails.getUsername(), days));
    }
}