package com.giuseppesica.maney.user.service;

import com.giuseppesica.maney.user.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.giuseppesica.maney.user.model.User;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with the given username, email, and plain password.
     * @param username the desired username
     * @param email the user's email
     * @param plainPassword the user's plain text password
     * @return the registered User
     * @throws IllegalArgumentException if the email is already in use
     */
    public User register(String username, String email,
                         String plainPassword) {
        if (userRepository.existsByEmail(email)) {
            logger.error("Attempted to register with an email that is already in use: {}", email);
            throw new IllegalArgumentException("Email already in use");
        }

        String hash = passwordEncoder.encode(plainPassword);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hash);
        logger.info("Registering new user with email: {}", email);
        return userRepository.save(user);
    }

    /**
     * Find a user by their email.
     * @param email the email to search for
     * @return an Optional containing the User if found, or empty if not found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find a user by their username.
     * @param username the username to search for
     * @return an Optional containing the User if found, or empty if not found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Check if an email exists in the system.
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if a username exists in the system.
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Authenticate a user with email and password.
     * @param email the user's email
     * @param plainPassword the user's plain text password
     * @return the authenticated User if credentials are correct
     * @throws IllegalArgumentException if email not found or password is incorrect
     */
    public User authenticate(String email, String plainPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(plainPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return user;
    }

    public User UserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        Optional<User> user = userRepository.findByEmail(authentication.getName());
        return user.orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
