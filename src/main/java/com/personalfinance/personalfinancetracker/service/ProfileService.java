package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.ChangePasswordRequest;
import com.personalfinance.personalfinancetracker.dto.ProfileResponse;
import com.personalfinance.personalfinancetracker.dto.UpdateProfileRequest;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.DuplicateResourceException;
import com.personalfinance.personalfinancetracker.exception.InvalidCredentialsException;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles viewing and updating the authenticated user's profile,
 * including email updates and password changes.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves the authenticated user's profile information.
     *
     * @param username the authenticated user's username
     * @return the user's profile data
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    public ProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Updates the authenticated user's profile information including
     * email, first name, and last name. Rejects email changes if the
     * new email is already in use by another account.
     *
     * @param username the authenticated user's username
     * @param request the updated profile fields
     * @return the updated profile
     * @throws ResourceNotFoundException if the user doesn't exist
     * @throws DuplicateResourceException if the new email is already taken
     */
    public ProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getEmail() != null &&
                !user.getEmail().equals(request.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email is already in use");
        }

        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        userRepository.save(user);

        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Changes the authenticated user's password after verifying the
     * current password is correct.
     *
     * @param username the authenticated user's username
     * @param request the current and new passwords
     * @throws ResourceNotFoundException if the user doesn't exist
     * @throws InvalidCredentialsException if the current password is wrong
     */
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
