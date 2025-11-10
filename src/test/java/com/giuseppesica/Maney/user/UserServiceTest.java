package com.giuseppesica.Maney.user.service;

import com.giuseppesica.Maney.user.domain.User;
import com.giuseppesica.Maney.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        // Initialize the mocks
        MockitoAnnotations.openMocks(this);

        // Prepare the test data
        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_password");
    }

    @Test
    public void testRegister_withPassword() {
        // Given
        String username = "john_doe";
        String email = "john@example.com";
        String plainPassword = "password123";

        // When
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(plainPassword)).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = userService.register(username, email, plainPassword);

        // Then
        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());
        assertEquals(email, registeredUser.getEmail());
        assertEquals("hashed_password", registeredUser.getPasswordHash());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(plainPassword);  // Verify password encoding
    }

    @Test
    public void testRegister_withExistingEmail_shouldThrowException() {
        // Given
        String email = "john@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register("john_doe", email, "password123");
        });
        assertEquals("Email already in use", exception.getMessage());
    }
}
