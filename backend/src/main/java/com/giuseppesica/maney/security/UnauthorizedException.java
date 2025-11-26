package com.giuseppesica.maney.security;

/**
 * Exception thrown when a user is not authenticated.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

