package com.personalfinance.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response payload returned after login or registration is successful.
 * Carries JWT the client should attach to future requests, also with
 * basic user info for display.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String email;
}
