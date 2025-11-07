package com.giuseppesica.Maney.user.controller;

import com.giuseppesica.Maney.user.dto.UserRegistrationDto;
import com.giuseppesica.Maney.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.giuseppesica.Maney.user.domain.User;
import com.giuseppesica.Maney.user.dto.UserResponseDto;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> registration(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        User user = userService.register(
                registrationDto.getUsername(),
                registrationDto.getEmail(),
                registrationDto.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDto(user));
    }
}
