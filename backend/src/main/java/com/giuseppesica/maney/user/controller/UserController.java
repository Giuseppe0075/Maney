package com.giuseppesica.maney.user.controller;

import com.giuseppesica.maney.portfolio.dto.Portfolio;
import com.giuseppesica.maney.user.dto.UserRegistrationDto;
import com.giuseppesica.maney.user.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.giuseppesica.maney.user.domain.User;
import com.giuseppesica.maney.user.dto.UserResponseDto;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<UserResponseDto> registration(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        User user = userService.register(
                registrationDto.getUsername(),
                registrationDto.getEmail(),
                registrationDto.getPassword()
        );
        Portfolio portfolio = new Portfolio();
        user.setPortfolio(portfolio);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDto(user));
    }
}
