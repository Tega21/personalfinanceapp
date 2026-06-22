package com.personalfinance.personalfinancetracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for authenticating an existing user. Used
 * by POST /api/auth/login
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
