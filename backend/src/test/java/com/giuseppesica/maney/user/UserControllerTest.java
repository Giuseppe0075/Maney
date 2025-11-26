package com.giuseppesica.maney.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.service.PortfolioService;
import com.giuseppesica.maney.security.AuthenticationHelper;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.dto.UserLoginDto;
import com.giuseppesica.maney.user.dto.UserRegistrationDto;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private IlliquidAssetService illiquidAssetService;

    @MockitoBean
    private LiquidityAccountService liquidityAccountService;

    @MockitoBean
    private AuthenticationHelper authenticationHelper;

    private User user;
    private Portfolio portfolio;

    @BeforeEach
    public void setUp() {
        // Sample user data for testing
        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_password");

        // Sample portfolio data
        portfolio = new Portfolio();
        portfolio.setId(1L);
        user.setPortfolio(portfolio);
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

    // ==================== GET PORTFOLIO TESTS ====================

    @Test
    @WithMockUser(username = "john@example.com")
    public void testGetPortfolio_Success_ReturnsPortfolioWithAssets() throws Exception {
        // Given
        List<IlliquidAssetDto> assets = new ArrayList<>();
        IlliquidAssetDto asset1 = new IlliquidAssetDto();
        asset1.setId(1L);
        asset1.setName("Vintage Car");
        asset1.setDescription("1967 Mustang");
        asset1.setEstimatedValue(75000.0f);
        assets.add(asset1);

        when(authenticationHelper.getAuthenticatedUserPortfolio(any())).thenReturn(portfolio);
        when(illiquidAssetService.getIlliquidAssets(1L)).thenReturn(assets);
        when(liquidityAccountService.getLiquidityAccounts(1L)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/user/portfolio")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.illiquidAssets").isArray())
                .andExpect(jsonPath("$.illiquidAssets[0].id").value(1))
                .andExpect(jsonPath("$.illiquidAssets[0].name").value("Vintage Car"));

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any());
        verify(illiquidAssetService, times(1)).getIlliquidAssets(1L);
        verify(liquidityAccountService, times(1)).getLiquidityAccounts(1L);
    }

    @Test
    @WithMockUser(username = "john@example.com")
    public void testGetPortfolio_UserHasNoPortfolio_ReturnsNotFound() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolio(any()))
                .thenThrow(new NotFoundException("Portfolio not found for user: john@example.com"));

        // When & Then
        mockMvc.perform(get("/user/portfolio")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Portfolio not found for user: john@example.com"));

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any());
        verify(illiquidAssetService, never()).getIlliquidAssets(any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    public void testGetPortfolio_PortfolioNotFoundInDb_ReturnsNotFound() throws Exception {
        // Given - AuthenticationHelper throws NotFoundException when portfolio not found
        when(authenticationHelper.getAuthenticatedUserPortfolio(any()))
                .thenThrow(new NotFoundException("Portfolio not found for user: john@example.com"));

        // When & Then
        mockMvc.perform(get("/user/portfolio")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any());
        verify(illiquidAssetService, never()).getIlliquidAssets(any());
    }

    @Test
    public void testGetPortfolio_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then - SecurityConfig returns 401 Unauthorized for unauthenticated requests
        mockMvc.perform(get("/user/portfolio"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        verify(userService, never()).UserFromAuthentication(any());
        verify(portfolioService, never()).findById(any());
        verify(illiquidAssetService, never()).getIlliquidAssets(any());
    }
}
