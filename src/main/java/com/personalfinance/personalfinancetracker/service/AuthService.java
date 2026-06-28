package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.AuthResponse;
import com.personalfinance.personalfinancetracker.dto.LoginRequest;
import com.personalfinance.personalfinancetracker.dto.RegisterRequest;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.DuplicateResourceException;
import com.personalfinance.personalfinancetracker.exception.InvalidCredentialsException;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import com.personalfinance.personalfinancetracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user authentication like registration, login, and JWT issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CategoryService categoryService;

    /**
     * Registers a new user account. Rejects the request if the username
     * or email is already taken, hashes the password with BCrypt, and
     * seeds the new account with 15 default categories before issuing
     * a JWT.
     *
     * @param request the new account's username, email, and password
     * @return an AuthResponse containing the JWT and the new user's info
     * @throws DuplicateResourceException if the username or email is already in use
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        categoryService.seedDefaultCategories(user);

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }

    /**
     * Authenticates a user and issues a JWT on success. Uses the same
     * generic exception for both an unknown username and a wrong
     * password, so the response doesn't reveal which one was incorrect.
     *
     * @param request the login credentials (username and password)
     * @return an AuthResponse containing the JWT and the user's info
     * @throws InvalidCredentialsException if the username or password is incorrect
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }
}
