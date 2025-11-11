package com.giuseppesica.maney.user.dto;

import lombok.Getter;
import com.giuseppesica.maney.user.domain.User;

import java.time.Instant;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String username;
    private final String email;
    private final Instant createdAt;

    public UserResponseDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }
}
