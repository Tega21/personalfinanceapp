package com.personalfinance.personalfinancetracker.exception;

/**
 * Thrown when a request would create a resource that already exists
 * (e.g., a duplicate username, email, or category name).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
