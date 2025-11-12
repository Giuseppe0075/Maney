package com.giuseppesica.maney.user.controller;

import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.user.dto.UserRegistrationDto;
import com.giuseppesica.maney.user.dto.UserLoginDto;
import com.giuseppesica.maney.user.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.dto.UserResponseDto;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Authenticates a user and initiates a session.
     * Spring Security handles session creation after successful authentication.
     *
     * @param loginDto the login credentials (email and password)
     * @return authenticated user information
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(
            @Valid @RequestBody UserLoginDto loginDto) {
        User user = userService.authenticate(loginDto.getEmail(), loginDto.getPassword());
        return ResponseEntity.ok(new UserResponseDto(user));
    }

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<UserResponseDto> registration(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        logger.info("Registering user with email: {}", registrationDto.getEmail());
        User user = userService.register(
                registrationDto.getUsername(),
                registrationDto.getEmail(),
                registrationDto.getPassword()
        );
        logger.info("User registered with ID: {}", user.getId());
        Portfolio portfolio = new Portfolio();
        user.setPortfolio(portfolio);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDto(user));
    }
}
