package com.giuseppesica.maney.user.controller;

import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.user.dto.UserRegistrationDto;
import com.giuseppesica.maney.user.dto.UserLoginDto;
import com.giuseppesica.maney.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.dto.UserResponseDto;

import java.util.List;

/**
 * REST controller for user management.
 * Handles user authentication, registration, and user-related operations.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Constructor for dependency injection.
     *
     * @param userService Service for user operations
     */
    @Autowired
    public UserController(
            UserService userService) {
        this.userService = userService;
    }

    /**
     * Authenticates a user and initiates a session.
     * Spring Security handles session creation after successful authentication.
     *
     * @param loginDto Login credentials (email and password)
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @return ResponseEntity with authenticated user information
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody UserLoginDto loginDto,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        logger.info("POST /user/login - email={}", loginDto.getEmail());
        User user = userService.authenticate(loginDto.getEmail(), loginDto.getPassword());

        // Create authentication token
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Set security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Save context in session
        request.getSession(true);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        logger.info("Login OK - session id={}", request.getSession(false).getId());
        return ResponseEntity.ok(new UserResponseDto(user));
    }

    /**
     * Registers a new user account.
     * Creates a new user and associated portfolio.
     *
     * @param registrationDto Registration data (username, email, password)
     * @return ResponseEntity with created user information and status 201
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        logger.info("POST /user/register - new user registration with email: {}", registrationDto.getEmail());
        User user = userService.register(
                registrationDto.getUsername(),
                registrationDto.getEmail(),
                registrationDto.getPassword()
        );
        logger.info("User registered successfully with ID: {}", user.getId());

        // Create portfolio for new user
        Portfolio portfolio = new Portfolio();
        user.setPortfolio(portfolio);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDto(user));
    }
}
