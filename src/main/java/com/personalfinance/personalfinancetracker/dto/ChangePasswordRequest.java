package com.personalfinance.personalfinancetracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for changing the authenticated user's password.
 * Requires the current password to prevent unauthorized changes.
 */
@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}