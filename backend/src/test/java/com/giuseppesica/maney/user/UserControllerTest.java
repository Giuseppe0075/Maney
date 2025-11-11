package com.giuseppesica.maney.user;

import com.giuseppesica.maney.user.controller.UserController;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.dto.UserRegistrationDto;
import com.giuseppesica.maney.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // Sample user data for testing
        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_password");
    }

    @Test
    public void testRegister_withPassword() throws Exception {
        // Given
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("john_doe");
        registrationDto.setEmail("john@example.com");
        registrationDto.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_password");

        when(userService.register(eq("john_doe"), eq("john@example.com"), eq("password123"))).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"password123\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).register(eq("john_doe"), eq("john@example.com"), eq("password123"));
    }

    @Test
    public void testRegister_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("john_doe");
        registrationDto.setEmail("john@example.com");
        registrationDto.setPassword("password123");

        when(userService.register(eq("john_doe"), eq("john@example.com"), eq("password123"))).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"john_doe\", \"email\": \"john@example.com\" }"))  // Missing password
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegister_withExistingEmail_shouldReturnBadRequest() throws Exception {
        // Given
        String email = "existing_user@example.com";
        when(userService.emailExists(email)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"existing_user@example.com\", \"password\": \"password123\" }"))
                .andExpect(status().isBadRequest());
    }
}
