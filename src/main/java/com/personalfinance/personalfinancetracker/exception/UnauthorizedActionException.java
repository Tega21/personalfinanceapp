package com.personalfinance.personalfinancetracker.exception;

/**
 * Thrown when a user attempts to modify or delete a transaction,
 * category that belongs to a different user.
 */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}