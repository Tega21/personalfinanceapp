package com.personalfinance.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload for the authenticated user's profile information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}