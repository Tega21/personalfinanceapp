package com.personalfinance.personalfinancetracker.exception;

/**
 * Thrown when login credentials (username or password) are invalid.
 * Uses a single generic exception for both "username not found" and
 * "wrong password" cases, so the response doesn't reveal which one
 * was incorrect.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}