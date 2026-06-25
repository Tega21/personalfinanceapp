package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.AuthResponse;
import com.personalfinance.personalfinancetracker.dto.LoginRequest;
import com.personalfinance.personalfinancetracker.dto.RegisterRequest;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.DuplicateResourceException;
import com.personalfinance.personalfinancetracker.exception.InvalidCredentialsException;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import com.personalfinance.personalfinancetracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    // Checks the "everything goes right" path: a brand-new username and email,
    // so registration should succeed and hand back a token.
    @Test
    void register_withValidData_returnsAuthResponse() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword123");

        User savedUser = User.builder()
                .id(1L)
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password("encodedPassword123")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(anyString())).thenReturn("fake-jwt-token-xyz");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token-xyz", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        verify(categoryService).seedDefaultCategories(any(User.class));
    }

    // Checks that registering with a username that's already taken is rejected,
    // and that no user actually gets saved when that happens.
    @Test
    void register_withDuplicateUsername_throwsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    // Same idea, but for a duplicate email instead of a duplicate username.
    @Test
    void register_withDuplicateEmail_throwsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    // Checks the "everything goes right" path for login.
    @Test
    void login_withValidCredentials_returnsAuthResponse() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword123")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true);
        when(jwtService.generateToken("testuser")).thenReturn("fake-jwt-token-xyz");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token-xyz", response.getToken());
        assertEquals("testuser", response.getUsername());
    }

    // Checks that a username that doesn't exist is rejected with the same
    // generic exception as a wrong password (so an attacker can't tell which one was wrong).
    @Test
    void login_withNonExistentUsername_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    // Checks that a real username with the WRONG password is also rejected.
    @Test
    void login_withWrongPassword_throwsException() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword123")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }
}