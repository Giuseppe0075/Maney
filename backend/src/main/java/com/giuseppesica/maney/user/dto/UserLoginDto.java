package com.giuseppesica.maney.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for user login requests.
 * Contains email and password required for authentication.
 */
@NoArgsConstructor
@Getter
@Setter
public class UserLoginDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}

