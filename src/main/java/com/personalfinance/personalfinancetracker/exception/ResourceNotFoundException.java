package com.personalfinance.personalfinancetracker.exception;

/**
 * Thrown when a requested resource (e.g., a user, transaction, or
 * category) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
