package com.giuseppesica.maney.user.dto;

import lombok.Getter;
import com.giuseppesica.maney.user.model.User;

import java.time.Instant;

/**
 * Data Transfer Object for user response data.
 * Contains safe user information to return to clients.
 * Excludes sensitive data like password hash.
 */
@Getter
public class UserResponseDto {

    /**
     * User's unique identifier.
     */
    private final Long id;

    /**
     * User's username.
     */
    private final String username;

    /**
     * User's email address.
     */
    private final String email;

    /**
     * Timestamp when the user account was created.
     */
    private final Instant createdAt;

    /**
     * Constructor to create a DTO from a User entity.
     * Only includes safe, non-sensitive user information.
     *
     * @param user The User entity to convert
     */
    public UserResponseDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }
}
