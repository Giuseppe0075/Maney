package com.giuseppesica.maney.illiquidasset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giuseppesica.maney.illiquidasset.controller.IlliquidAssetController;
import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.service.PortfolioService;
import com.giuseppesica.maney.security.AuthenticationHelper;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for IlliquidAssetController.
 * Tests all REST endpoints for managing illiquid assets.
 * Uses MockMvc to simulate HTTP requests without starting the full server.
 */
@WebMvcTest(IlliquidAssetController.class)
public class IlliquidAssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IlliquidAssetService illiquidAssetService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private AuthenticationHelper authenticationHelper;

    private User testUser;
    private Portfolio testPortfolio;
    private IlliquidAsset testAsset;

    @BeforeEach
    public void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        // Create test portfolio
        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setUser(testUser);

        // Link portfolio to user
        testUser.setPortfolio(testPortfolio);

        // Create test illiquid asset
        testAsset = new IlliquidAsset();
        testAsset.setId(1L);
        testAsset.setName("Real Estate");
        testAsset.setDescription("Downtown apartment");
        testAsset.setEstimatedValue(250000.0f);
        testAsset.setPortfolio(testPortfolio);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetIlliquidAsset_AssetExists_ReturnsAsset() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class))).thenReturn(1L);
        when(illiquidAssetService.getIlliquidAssetById(1L, 1L)).thenReturn(Optional.of(testAsset));

        // When & Then
        mockMvc.perform(get("/user/illiquid-asset/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Real Estate"))
                .andExpect(jsonPath("$.description").value("Downtown apartment"))
                .andExpect(jsonPath("$.estimatedValue").value(250000.0));

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, times(1)).getIlliquidAssetById(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetIlliquidAsset_AssetDoesNotExist_Returns404() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class))).thenReturn(1L);
        when(illiquidAssetService.getIlliquidAssetById(1L, 99L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/user/illiquid-asset/{id}", 99L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, times(1)).getIlliquidAssetById(1L, 99L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetIlliquidAsset_UserNotFound_Returns404() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class)))
                .thenThrow(new NotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/user/illiquid-asset/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, never()).getIlliquidAssetById(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetIlliquidAsset_PortfolioNotFound_Returns404() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class)))
                .thenThrow(new NotFoundException("Portfolio not found"));

        // When & Then
        mockMvc.perform(get("/user/illiquid-asset/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, never()).getIlliquidAssetById(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateIlliquidAsset_Success_ReturnsCreated() throws Exception {
        // Given
        IlliquidAssetDto newAssetDto = new IlliquidAssetDto();
        newAssetDto.setName("Vintage Car");
        newAssetDto.setDescription("1967 Mustang");
        newAssetDto.setEstimatedValue(75000.0f);

        IlliquidAsset createdAsset = new IlliquidAsset(newAssetDto);
        createdAsset.setId(2L);
        createdAsset.setPortfolio(testPortfolio);

        when(authenticationHelper.getAuthenticatedUserPortfolio(any(Authentication.class))).thenReturn(testPortfolio);
        when(illiquidAssetService.createIlliquidAsset(any(IlliquidAssetDto.class), eq(testPortfolio)))
                .thenReturn(createdAsset);

        // When & Then
        mockMvc.perform(post("/user/illiquid-asset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssetDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Vintage Car"))
                .andExpect(jsonPath("$.description").value("1967 Mustang"))
                .andExpect(jsonPath("$.estimatedValue").value(75000.0));

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any(Authentication.class));
        verify(illiquidAssetService, times(1)).createIlliquidAsset(any(IlliquidAssetDto.class), eq(testPortfolio));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateIlliquidAsset_UserNotFound_Returns404() throws Exception {
        // Given
        IlliquidAssetDto newAssetDto = new IlliquidAssetDto();
        newAssetDto.setName("Vintage Car");
        newAssetDto.setDescription("1967 Mustang");
        newAssetDto.setEstimatedValue(75000.0f);

        when(authenticationHelper.getAuthenticatedUserPortfolio(any(Authentication.class)))
                .thenThrow(new NotFoundException("User not found"));

        // When & Then
        mockMvc.perform(post("/user/illiquid-asset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssetDto)))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any(Authentication.class));
        verify(illiquidAssetService, never()).createIlliquidAsset(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateIlliquidAsset_PortfolioNotFound_Returns404() throws Exception {
        // Given
        IlliquidAssetDto newAssetDto = new IlliquidAssetDto();
        newAssetDto.setName("Vintage Car");
        newAssetDto.setDescription("1967 Mustang");
        newAssetDto.setEstimatedValue(75000.0f);

        when(authenticationHelper.getAuthenticatedUserPortfolio(any(Authentication.class)))
                .thenThrow(new NotFoundException("Portfolio not found"));

        // When & Then
        mockMvc.perform(post("/user/illiquid-asset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssetDto)))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any(Authentication.class));
        verify(illiquidAssetService, never()).createIlliquidAsset(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateIlliquidAsset_Success_ReturnsUpdated() throws Exception {
        // Given
        IlliquidAssetDto updateDto = new IlliquidAssetDto();
        updateDto.setName("Updated Real Estate");
        updateDto.setDescription("Renovated apartment");
        updateDto.setEstimatedValue(300000.0f);

        IlliquidAsset updatedAsset = new IlliquidAsset();
        updatedAsset.setId(1L);
        updatedAsset.setName("Updated Real Estate");
        updatedAsset.setDescription("Renovated apartment");
        updatedAsset.setEstimatedValue(300000.0f);
        updatedAsset.setPortfolio(testPortfolio);

        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class))).thenReturn(1L);
        when(illiquidAssetService.updateIlliquidAsset(eq(1L), eq(1L), any(IlliquidAssetDto.class)))
                .thenReturn(Optional.of(updatedAsset));

        // When & Then
        mockMvc.perform(put("/user/illiquid-asset/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Real Estate"))
                .andExpect(jsonPath("$.description").value("Renovated apartment"))
                .andExpect(jsonPath("$.estimatedValue").value(300000.0));

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, times(1)).updateIlliquidAsset(eq(1L), eq(1L), any(IlliquidAssetDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateIlliquidAsset_AssetNotFound_Returns404() throws Exception {
        // Given
        IlliquidAssetDto updateDto = new IlliquidAssetDto();
        updateDto.setName("Updated Asset");
        updateDto.setDescription("Description");
        updateDto.setEstimatedValue(100000.0f);

        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class))).thenReturn(1L);
        when(illiquidAssetService.updateIlliquidAsset(eq(1L), eq(99L), any(IlliquidAssetDto.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/user/illiquid-asset/{id}", 99L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, times(1)).updateIlliquidAsset(eq(1L), eq(99L), any(IlliquidAssetDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateIlliquidAsset_UserNotFound_Returns404() throws Exception {
        // Given
        IlliquidAssetDto updateDto = new IlliquidAssetDto();
        updateDto.setName("Updated Asset");
        updateDto.setDescription("Description");
        updateDto.setEstimatedValue(100000.0f);

        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class)))
                .thenThrow(new NotFoundException("User not found"));

        // When & Then
        mockMvc.perform(put("/user/illiquid-asset/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, never()).updateIlliquidAsset(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateIlliquidAsset_PortfolioNotFound_Returns404() throws Exception {
        // Given
        IlliquidAssetDto updateDto = new IlliquidAssetDto();
        updateDto.setName("Updated Asset");
        updateDto.setDescription("Description");
        updateDto.setEstimatedValue(100000.0f);

        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class)))
                .thenThrow(new NotFoundException("Portfolio not found"));

        // When & Then
        mockMvc.perform(put("/user/illiquid-asset/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, never()).updateIlliquidAsset(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteIlliquidAsset_Success_ReturnsNoContent() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class))).thenReturn(1L);
        when(illiquidAssetService.getIlliquidAssetById(1L, 1L)).thenReturn(Optional.of(testAsset));
        doNothing().when(illiquidAssetService).deleteIlliquidAsset(testAsset);

        // When & Then
        mockMvc.perform(delete("/user/illiquid-asset/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, times(1)).getIlliquidAssetById(1L, 1L);
        verify(illiquidAssetService, times(1)).deleteIlliquidAsset(testAsset);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteIlliquidAsset_AssetNotFound_Returns404() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class))).thenReturn(1L);
        when(illiquidAssetService.getIlliquidAssetById(1L, 99L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/user/illiquid-asset/{id}", 99L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, times(1)).getIlliquidAssetById(1L, 99L);
        verify(illiquidAssetService, never()).deleteIlliquidAsset(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteIlliquidAsset_UserNotFound_Returns404() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class)))
                .thenThrow(new NotFoundException("User not found"));

        // When & Then
        mockMvc.perform(delete("/user/illiquid-asset/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, never()).getIlliquidAssetById(any(), any());
        verify(illiquidAssetService, never()).deleteIlliquidAsset(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteIlliquidAsset_PortfolioNotFound_Returns404() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolioId(any(Authentication.class)))
                .thenThrow(new NotFoundException("Portfolio not found"));

        // When & Then
        mockMvc.perform(delete("/user/illiquid-asset/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolioId(any(Authentication.class));
        verify(illiquidAssetService, never()).getIlliquidAssetById(any(), any());
        verify(illiquidAssetService, never()).deleteIlliquidAsset(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateIlliquidAsset_WithNullDescription_Success() throws Exception {
        // Given
        IlliquidAssetDto newAssetDto = new IlliquidAssetDto();
        newAssetDto.setName("Simple Asset");
        newAssetDto.setDescription(null);
        newAssetDto.setEstimatedValue(1000.0f);

        IlliquidAsset createdAsset = new IlliquidAsset(newAssetDto);
        createdAsset.setId(3L);
        createdAsset.setPortfolio(testPortfolio);

        when(authenticationHelper.getAuthenticatedUserPortfolio(any(Authentication.class))).thenReturn(testPortfolio);
        when(illiquidAssetService.createIlliquidAsset(any(IlliquidAssetDto.class), eq(testPortfolio)))
                .thenReturn(createdAsset);

        // When & Then
        mockMvc.perform(post("/user/illiquid-asset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssetDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Simple Asset"))
                .andExpect(jsonPath("$.estimatedValue").value(1000.0));

        verify(illiquidAssetService, times(1)).createIlliquidAsset(any(IlliquidAssetDto.class), eq(testPortfolio));
    }

    @Test
    public void testGetIlliquidAsset_Unauthorized_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/user/illiquid-asset/{id}", 1L))
                .andExpect(status().isUnauthorized());

        verify(authenticationHelper, never()).getAuthenticatedUserPortfolioId(any());
        verify(illiquidAssetService, never()).getIlliquidAssetById(any(), any());
    }

    @Test
    public void testCreateIlliquidAsset_Forbidden_Returns403() throws Exception {
        // Given
        IlliquidAssetDto newAssetDto = new IlliquidAssetDto();
        newAssetDto.setName("Vintage Car");
        newAssetDto.setDescription("1967 Mustang");
        newAssetDto.setEstimatedValue(75000.0f);

        // When & Then
        mockMvc.perform(post("/user/illiquid-asset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssetDto)))
                .andExpect(status().isForbidden());

        verify(authenticationHelper, never()).getAuthenticatedUserPortfolio(any());
        verify(illiquidAssetService, never()).createIlliquidAsset(any(), any());
    }

    @Test
    public void testUpdateIlliquidAsset_Forbidden_Returns403() throws Exception {
        // Given
        IlliquidAssetDto updateDto = new IlliquidAssetDto();
        updateDto.setName("Updated Asset");
        updateDto.setDescription("Description");
        updateDto.setEstimatedValue(100000.0f);

        // When & Then
        mockMvc.perform(put("/user/illiquid-asset/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

        verify(authenticationHelper, never()).getAuthenticatedUserPortfolioId(any());
        verify(illiquidAssetService, never()).updateIlliquidAsset(any(), any(), any());
    }

    @Test
    public void testDeleteIlliquidAsset_Forbidden_Returns403() throws Exception {
        // When & Then
        mockMvc.perform(delete("/user/illiquid-asset/{id}", 1L))
                .andExpect(status().isForbidden());

        verify(authenticationHelper, never()).getAuthenticatedUserPortfolioId(any());
        verify(illiquidAssetService, never()).getIlliquidAssetById(any(), any());
        verify(illiquidAssetService, never()).deleteIlliquidAsset(any());
    }
}

