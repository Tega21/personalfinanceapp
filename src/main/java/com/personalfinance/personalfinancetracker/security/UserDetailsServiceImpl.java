package com.personalfinance.personalfinancetracker.security;

import com.personalfinance.personalfinancetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges the application's User entity with Spring Security's
 * authentication system. User already implements UserDetails directly,
 * so this service simply looks the user up by username for Spring
 * Security to use during authentication.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by username for Spring Security's authentication process.
     *
     * @param username the username to look up
     * @return the matching user, which implements UserDetails
     * @throws UsernameNotFoundException if no user exists with that username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
    }
}