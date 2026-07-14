package com.personalfinance.personalfinancetracker.controller;

import com.personalfinance.personalfinancetracker.dto.ChangePasswordRequest;
import com.personalfinance.personalfinancetracker.dto.ProfileResponse;
import com.personalfinance.personalfinancetracker.dto.UpdateProfileRequest;
import com.personalfinance.personalfinancetracker.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes profile-related REST endpoints under /api/profile.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Returns the authenticated user's profile information.
     *
     * @param userDetails the authenticated user, injected from the JWT
     * @return 200 OK with the user's profile
     */
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                profileService.getProfile(userDetails.getUsername())
        );
    }

    /**
     * Updates the authenticated user's email address.
     *
     * @param request the new email address
     * @param userDetails the authenticated user, injected from the JWT
     * @return 200 OK with the updated profile
     */
    @PutMapping("/email")
    public ResponseEntity<ProfileResponse> updateEmail(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                profileService.updateEmail(userDetails.getUsername(), request)
        );
    }

    /**
     * Changes the authenticated user's password.
     *
     * @param request the current and new passwords
     * @param userDetails the authenticated user, injected from the JWT
     * @return 204 No Content on success
     */
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        profileService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }
}
