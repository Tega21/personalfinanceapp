package com.personalfinance.personalfinancetracker.service;

import com.personalfinance.personalfinancetracker.dto.AuthResponse;
import com.personalfinance.personalfinancetracker.dto.LoginRequest;
import com.personalfinance.personalfinancetracker.dto.RegisterRequest;
import com.personalfinance.personalfinancetracker.entity.User;
import com.personalfinance.personalfinancetracker.exception.DuplicateResourceException;
import com.personalfinance.personalfinancetracker.exception.ResourceNotFoundException;
import com.personalfinance.personalfinancetracker.repository.UserRepository;
import com.personalfinance.personalfinancetracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register (RegisterRequest request){
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

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }

    public AuthResponse login (LoginRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }
}
