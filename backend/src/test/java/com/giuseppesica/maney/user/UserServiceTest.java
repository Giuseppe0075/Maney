package com.giuseppesica.maney.user;

import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.model.UserRepository;
import com.giuseppesica.maney.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

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

    // ==================== REGISTER TESTS ====================

    @Test
    public void testRegister_Success_ReturnsUser() {
        // Given
        String username = "john_doe";
        String email = "john@example.com";
        String plainPassword = "password123";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(plainPassword)).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User registeredUser = userService.register(username, email, plainPassword);

        // Then
        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());
        assertEquals(email, registeredUser.getEmail());
        assertEquals("hashed_password", registeredUser.getPasswordHash());
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(plainPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegister_EmailAlreadyExists_ThrowsException() {
        // Given
        String email = "john@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.register("john_doe", email, "password123"));
        assertEquals("Email already in use", exception.getMessage());

        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    // ==================== AUTHENTICATE TESTS ====================

    @Test
    public void testAuthenticate_Success_ReturnsUser() {
        // Given
        String email = "john@example.com";
        String plainPassword = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(plainPassword, "hashed_password")).thenReturn(true);

        // When
        User authenticatedUser = userService.authenticate(email, plainPassword);

        // Then
        assertNotNull(authenticatedUser);
        assertEquals(user.getId(), authenticatedUser.getId());
        assertEquals(user.getEmail(), authenticatedUser.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(plainPassword, "hashed_password");
    }

    @Test
    public void testAuthenticate_UserNotFound_ThrowsException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.authenticate(email, "password123"));
        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    public void testAuthenticate_InvalidPassword_ThrowsException() {
        // Given
        String email = "john@example.com";
        String wrongPassword = "wrongpassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, "hashed_password")).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.authenticate(email, wrongPassword));
        assertEquals("Invalid password", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(wrongPassword, "hashed_password");
    }

    // ==================== FIND BY EMAIL TESTS ====================

    @Test
    public void testFindByEmail_UserExists_ReturnsUser() {
        // Given
        String email = "john@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testFindByEmail_UserNotExists_ReturnsEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
    }

    // ==================== FIND BY USERNAME TESTS ====================

    @Test
    public void testFindByUsername_UserExists_ReturnsUser() {
        // Given
        String username = "john_doe";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(user.getUsername(), result.get().getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    public void testFindByUsername_UserNotExists_ReturnsEmpty() {
        // Given
        String username = "nonexistent_user";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername(username);
    }

    // ==================== EMAIL/USERNAME EXISTS TESTS ====================

    @Test
    public void testEmailExists_EmailExists_ReturnsTrue() {
        // Given
        String email = "john@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = userService.emailExists(email);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    public void testEmailExists_EmailNotExists_ReturnsFalse() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When
        boolean result = userService.emailExists(email);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    public void testUsernameExists_UsernameExists_ReturnsTrue() {
        // Given
        String username = "john_doe";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When
        boolean result = userService.usernameExists(username);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername(username);
    }

    @Test
    public void testUsernameExists_UsernameNotExists_ReturnsFalse() {
        // Given
        String username = "nonexistent_user";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        // When
        boolean result = userService.usernameExists(username);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).existsByUsername(username);
    }

    // ==================== USER FROM AUTHENTICATION TESTS ====================

    @Test
    public void testUserFromAuthentication_Success_ReturnsUser() {
        // Given
        String email = "john@example.com";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        User result = userService.UserFromAuthentication(authentication);

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testUserFromAuthentication_NotAuthenticated_ThrowsException() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.UserFromAuthentication(authentication));
        assertEquals("User is not authenticated", exception.getMessage());

        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, never()).getName();
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    public void testUserFromAuthentication_NullAuthentication_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.UserFromAuthentication(null));
        assertEquals("User is not authenticated", exception.getMessage());

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    public void testUserFromAuthentication_UserNotFound_ThrowsException() {
        // Given
        String email = "nonexistent@example.com";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.UserFromAuthentication(authentication));
        assertEquals("User not found", exception.getMessage());

        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByEmail(email);
    }
}
