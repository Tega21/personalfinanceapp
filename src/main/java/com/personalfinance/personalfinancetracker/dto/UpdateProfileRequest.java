package com.personalfinance.personalfinancetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for updating the authenticated user's email address.
 */
@Data
public class UpdateProfileRequest {
    @Email(message = "Must be a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    private String firstName;
    private String lastName;
}