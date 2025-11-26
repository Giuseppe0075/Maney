package com.giuseppesica.maney.security;

/**
 * Exception thrown when a user tries to access a resource they don't have permission for.
 * Maps to HTTP 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

