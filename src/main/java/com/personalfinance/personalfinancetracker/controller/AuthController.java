package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.AuthResponse;
import com.personalfinance.personalfinancetracker.dto.LoginRequest;
import com.personalfinance.personalfinancetracker.dto.RegisterRequest;
import com.personalfinance.personalfinancetracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes authentication REST endpoints under /api/auth.
 * Handles new user registration and login which are publicly
 * accessible since user can't be authenticated before logged in.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account
     *
     * @param request new user account's username, email, and password
     * @return 200 OK with AuthResponse containing JWT and new user's info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Authenticates an already existing user
     *
     * @param request username and password to authenticate with
     * @return 200 OK with AuthResponse containing JWT and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
