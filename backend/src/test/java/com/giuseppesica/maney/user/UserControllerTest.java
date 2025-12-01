package com.giuseppesica.maney.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giuseppesica.maney.config.SecurityConfig;
import com.giuseppesica.maney.user.controller.UserController;
import com.giuseppesica.maney.user.dto.UserLoginDto;
import com.giuseppesica.maney.user.dto.UserRegistrationDto;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        // Sample user data for testing
        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_password");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    public void testRegister_Success_ReturnsCreated() throws Exception {
        // Given
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("john_doe");
        registrationDto.setEmail("john@example.com");
        registrationDto.setPassword("password123");

        when(userService.register(eq("john_doe"), eq("john@example.com"), eq("password123")))
                .thenReturn(user);

        // When & Then
        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).register(eq("john_doe"), eq("john@example.com"), eq("password123"));
    }

    @Test
    public void testRegister_MissingPassword_ReturnsBadRequest() throws Exception {
        // Given - Missing password field
        String invalidJson = "{ \"username\": \"john_doe\", \"email\": \"john@example.com\" }";

        // When & Then
        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(), any(), any());
    }

    @Test
    public void testRegister_MissingEmail_ReturnsBadRequest() throws Exception {
        // Given - Missing email field
        String invalidJson = "{ \"username\": \"john_doe\", \"password\": \"password123\" }";

        // When & Then
        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(), any(), any());
    }

    @Test
    public void testRegister_EmailAlreadyExists_ReturnsBadRequest() throws Exception {
        // Given
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("john_doe");
        registrationDto.setEmail("existing@example.com");
        registrationDto.setPassword("password123");

        when(userService.register(any(), eq("existing@example.com"), any()))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        // When & Then
        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Email already in use"));

        verify(userService, times(1)).register(any(), any(), any());
    }

    @Test
    public void testRegister_UsernameTooShort_ReturnsBadRequest() throws Exception {
        // Given - Username less than 3 characters
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("ab");
        registrationDto.setEmail("john@example.com");
        registrationDto.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(), any(), any());
    }

    @Test
    public void testRegister_PasswordTooShort_ReturnsBadRequest() throws Exception {
        // Given - Password less than 8 characters
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("john_doe");
        registrationDto.setEmail("john@example.com");
        registrationDto.setPassword("pass123");

        // When & Then
        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(), any(), any());
    }

    @Test
    public void testRegister_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given - Invalid email format
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("john_doe");
        registrationDto.setEmail("invalid-email");
        registrationDto.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(), any(), any());
    }

    @Test
    public void testRegister_MissingUsername_ReturnsBadRequest() throws Exception {
        // Given - Missing username field
        String invalidJson = "{ \"email\": \"john@example.com\", \"password\": \"password123\" }";

        // When & Then
        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(), any(), any());
    }

    // ==================== LOGIN TESTS ====================

    @Test
    public void testLogin_Success_ReturnsOk() throws Exception {
        // Given
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("john@example.com");
        loginDto.setPassword("password123");

        when(userService.authenticate(eq("john@example.com"), eq("password123")))
                .thenReturn(user);

        // When & Then
        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).authenticate(eq("john@example.com"), eq("password123"));
    }

    @Test
    public void testLogin_InvalidCredentials_ReturnsBadRequest() throws Exception {
        // Given
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("john@example.com");
        loginDto.setPassword("wrongpassword");

        when(userService.authenticate(eq("john@example.com"), eq("wrongpassword")))
                .thenThrow(new IllegalArgumentException("Invalid password"));

        // When & Then
        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid password"));

        verify(userService, times(1)).authenticate(eq("john@example.com"), eq("wrongpassword"));
    }

    @Test
    public void testLogin_MissingEmail_ReturnsBadRequest() throws Exception {
        // Given - Missing email field
        String invalidJson = "{ \"password\": \"password123\" }";

        // When & Then
        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).authenticate(any(), any());
    }

    @Test
    public void testLogin_MissingPassword_ReturnsBadRequest() throws Exception {
        // Given - Missing password field
        String invalidJson = "{ \"email\": \"john@example.com\" }";

        // When & Then
        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).authenticate(any(), any());
    }

    @Test
    public void testLogin_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("password123");

        when(userService.authenticate(eq("nonexistent@example.com"), eq("password123")))
                .thenThrow(new IllegalArgumentException("User not found"));

        // When & Then
        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService, times(1)).authenticate(eq("nonexistent@example.com"), eq("password123"));
    }

    @Test
    public void testLogin_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given - Invalid email format
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("invalid-email");
        loginDto.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).authenticate(any(), any());
    }
}
