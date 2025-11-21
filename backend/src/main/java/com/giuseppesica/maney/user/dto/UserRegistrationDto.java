package com.giuseppesica.maney.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for user registration requests.
 * Contains all required fields for creating a new user account.
 */
@NoArgsConstructor
@Getter
@Setter
public class UserRegistrationDto {

    /**
     * Desired username for the new account.
     * Must be between 3 and 50 characters.
     */
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    /**
     * Email address for the new account.
     * Must be a valid email format.
     */
    @NotBlank
    @Email
    private String email;

    /**
     * Password for the new account.
     * Must be between 8 and 64 characters.
     */
    @NotBlank
    @Size(min = 8, max = 64)
    private String password;
}
