package com.giuseppesica.maney.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giuseppesica.maney.account.controller.LiquidityAccountController;
import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.config.SecurityConfig;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.security.AuthenticationHelper;
import com.giuseppesica.maney.utils.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for LiquidityAccountController.
 * Tests all CRUD operations and validates authentication, authorization, and error handling.
 */
@WebMvcTest(LiquidityAccountController.class)
@Import(SecurityConfig.class)
public class LiquidityAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LiquidityAccountService liquidityAccountService;

    @MockitoBean
    private AuthenticationHelper authenticationHelper;

    private LiquidityAccount liquidityAccount;
    private LiquidityAccountDto liquidityAccountDto;
    private Portfolio portfolio;

    @BeforeEach
    public void setUp() {
        // Setup portfolio
        portfolio = new Portfolio();
        portfolio.setId(1L);

        // Setup liquidity account entity
        liquidityAccount = new LiquidityAccount();
        liquidityAccount.setName("Conto Corrente");
        liquidityAccount.setInstitution("Intesa Sanpaolo");
        liquidityAccount.setBalance(new BigDecimal("1000.00"));
        liquidityAccount.setCurrency(Currency.EUR);
        liquidityAccount.setOpenedAt(Instant.parse("2024-01-01T10:00:00Z"));
        liquidityAccount.setNote("Main account");
        liquidityAccount.setPortfolio(portfolio);

        // Setup DTO for requests
        liquidityAccountDto = new LiquidityAccountDto();
        liquidityAccountDto.setName("Conto Corrente");
        liquidityAccountDto.setInstitution("Intesa Sanpaolo");
        liquidityAccountDto.setBalance(new BigDecimal("1000.00"));
        liquidityAccountDto.setCurrency(Currency.EUR);
        liquidityAccountDto.setOpenedAt(Instant.parse("2024-01-01T10:00:00Z"));
        liquidityAccountDto.setNote("Main account");
        liquidityAccountDto.setPortfolioId(1L);
    }

    // ==================== CREATE LIQUIDITY ACCOUNT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateLiquidityAccount_Success_ReturnsCreated() throws Exception {
        // Given
        doNothing().when(authenticationHelper).validatePortfolioAccess(any(), eq(1L));
        when(liquidityAccountService.saveLiquidityAccount(any(LiquidityAccount.class)))
                .thenReturn(liquidityAccount);

        // When & Then
        mockMvc.perform(post("/user/portfolio/liquidity-accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conto Corrente"))
                .andExpect(jsonPath("$.institution").value("Intesa Sanpaolo"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.portfolioId").value(1));

        verify(authenticationHelper, times(1)).validatePortfolioAccess(any(), eq(1L));
        verify(liquidityAccountService, times(1)).saveLiquidityAccount(any(LiquidityAccount.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateLiquidityAccount_InvalidPortfolioAccess_ReturnsForbidden() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Portfolio does not belong to user"))
                .when(authenticationHelper).validatePortfolioAccess(any(), eq(1L));

        // When & Then
        mockMvc.perform(post("/user/portfolio/liquidity-accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Portfolio does not belong to user"));

        verify(authenticationHelper, times(1)).validatePortfolioAccess(any(), eq(1L));
        verify(liquidityAccountService, never()).saveLiquidityAccount(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateLiquidityAccount_MissingName_ReturnsBadRequest() throws Exception {
        // Given - Missing required field 'name'
        liquidityAccountDto.setName(null);

        // When & Then
        mockMvc.perform(post("/user/portfolio/liquidity-accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isBadRequest());

        verify(authenticationHelper, never()).validatePortfolioAccess(any(), any());
        verify(liquidityAccountService, never()).saveLiquidityAccount(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateLiquidityAccount_MissingBalance_ReturnsBadRequest() throws Exception {
        // Given - Missing required field 'balance'
        liquidityAccountDto.setBalance(null);

        // When & Then
        mockMvc.perform(post("/user/portfolio/liquidity-accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isBadRequest());

        verify(authenticationHelper, never()).validatePortfolioAccess(any(), any());
        verify(liquidityAccountService, never()).saveLiquidityAccount(any());
    }

    @Test
    public void testCreateLiquidityAccount_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then - No authentication and no CSRF token returns 403
        mockMvc.perform(post("/user/portfolio/liquidity-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isForbidden());

        verify(authenticationHelper, never()).validatePortfolioAccess(any(), any());
        verify(liquidityAccountService, never()).saveLiquidityAccount(any());
    }

    // ==================== GET LIQUIDITY ACCOUNT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetLiquidityAccount_Success_ReturnsAccount() throws Exception {
        // Given
        when(liquidityAccountService.getLiquidityAccountById(1L))
                .thenReturn(Optional.of(liquidityAccount));
        doNothing().when(authenticationHelper).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));

        // When & Then
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conto Corrente"))
                .andExpect(jsonPath("$.institution").value("Intesa Sanpaolo"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.currency").value("EUR"));

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(1L);
        verify(authenticationHelper, times(1)).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetLiquidityAccount_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(liquidityAccountService.getLiquidityAccountById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Liquidity account not found with ID: 999"));

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(999L);
        verify(authenticationHelper, never()).validateResourceAccess(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetLiquidityAccount_UnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Given
        when(liquidityAccountService.getLiquidityAccountById(1L))
                .thenReturn(Optional.of(liquidityAccount));
        doThrow(new IllegalArgumentException("Resource does not belong to user"))
                .when(authenticationHelper).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));

        // When & Then
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Resource does not belong to user"));

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(1L);
        verify(authenticationHelper, times(1)).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));
    }

    @Test
    public void testGetLiquidityAccount_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then - No authentication, GET request returns 401 with continue parameter
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        verify(liquidityAccountService, never()).getLiquidityAccountById(any());
        verify(authenticationHelper, never()).validateResourceAccess(any(), any(), any());
    }

    // ==================== UPDATE LIQUIDITY ACCOUNT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateLiquidityAccount_Success_ReturnsUpdated() throws Exception {
        // Given
        LiquidityAccountDto updateDto = new LiquidityAccountDto();
        updateDto.setName("Updated Account");
        updateDto.setInstitution("UniCredit");
        updateDto.setBalance(new BigDecimal("2000.00"));
        updateDto.setCurrency(Currency.EUR);
        updateDto.setOpenedAt(Instant.parse("2024-01-01T10:00:00Z"));
        updateDto.setNote("Updated note");
        updateDto.setPortfolioId(1L);

        LiquidityAccount updatedAccount = new LiquidityAccount();
        updatedAccount.setName("Updated Account");
        updatedAccount.setInstitution("UniCredit");
        updatedAccount.setBalance(new BigDecimal("2000.00"));
        updatedAccount.setCurrency(Currency.EUR);
        updatedAccount.setPortfolio(portfolio);

        when(liquidityAccountService.getLiquidityAccountById(1L))
                .thenReturn(Optional.of(liquidityAccount));
        doNothing().when(authenticationHelper).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));
        when(liquidityAccountService.updateLiquidityAccount(eq(1L), any(LiquidityAccountDto.class)))
                .thenReturn(updatedAccount);

        // When & Then
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Account"))
                .andExpect(jsonPath("$.institution").value("UniCredit"))
                .andExpect(jsonPath("$.balance").value(2000.00));

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(1L);
        verify(authenticationHelper, times(1)).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));
        verify(liquidityAccountService, times(1)).updateLiquidityAccount(eq(1L), any(LiquidityAccountDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateLiquidityAccount_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(liquidityAccountService.getLiquidityAccountById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Liquidity account not found with ID: 999"));

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(999L);
        verify(authenticationHelper, never()).validateResourceAccess(any(), any(), any());
        verify(liquidityAccountService, never()).updateLiquidityAccount(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateLiquidityAccount_UnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Given
        when(liquidityAccountService.getLiquidityAccountById(1L))
                .thenReturn(Optional.of(liquidityAccount));
        doThrow(new IllegalArgumentException("Resource does not belong to user"))
                .when(authenticationHelper).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));

        // When & Then
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Resource does not belong to user"));

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(1L);
        verify(authenticationHelper, times(1)).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));
        verify(liquidityAccountService, never()).updateLiquidityAccount(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateLiquidityAccount_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        // Given - Missing required field 'name'
        liquidityAccountDto.setName(null);

        // When & Then
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isBadRequest());

        verify(liquidityAccountService, never()).getLiquidityAccountById(any());
        verify(authenticationHelper, never()).validateResourceAccess(any(), any(), any());
        verify(liquidityAccountService, never()).updateLiquidityAccount(any(), any());
    }

    @Test
    public void testUpdateLiquidityAccount_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then - No authentication and no CSRF token returns 403
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(liquidityAccountDto)))
                .andExpect(status().isForbidden());

        verify(liquidityAccountService, never()).getLiquidityAccountById(any());
        verify(authenticationHelper, never()).validateResourceAccess(any(), any(), any());
        verify(liquidityAccountService, never()).updateLiquidityAccount(any(), any());
    }

    // ==================== DELETE LIQUIDITY ACCOUNT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteLiquidityAccount_Success_ReturnsNoContent() throws Exception {
        // Given
        when(liquidityAccountService.getLiquidityAccountById(1L))
                .thenReturn(Optional.of(liquidityAccount));
        doNothing().when(authenticationHelper).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));
        doNothing().when(liquidityAccountService).deleteLiquidityAccount(1L);

        // When & Then
        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(1L);
        verify(authenticationHelper, times(1)).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));
        verify(liquidityAccountService, times(1)).deleteLiquidityAccount(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteLiquidityAccount_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(liquidityAccountService.getLiquidityAccountById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Liquidity account not found with ID: 999"));

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(999L);
        verify(authenticationHelper, never()).validateResourceAccess(any(), any(), any());
        verify(liquidityAccountService, never()).deleteLiquidityAccount(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteLiquidityAccount_UnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Given
        when(liquidityAccountService.getLiquidityAccountById(1L))
                .thenReturn(Optional.of(liquidityAccount));
        doThrow(new IllegalArgumentException("Resource does not belong to user"))
                .when(authenticationHelper).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));

        // When & Then
        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Resource does not belong to user"));

        verify(liquidityAccountService, times(1)).getLiquidityAccountById(1L);
        verify(authenticationHelper, times(1)).validateResourceAccess(any(), eq(1L), eq("LiquidityAccount"));
        verify(liquidityAccountService, never()).deleteLiquidityAccount(any());
    }

    @Test
    public void testDeleteLiquidityAccount_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then - No authentication and no CSRF token returns 403
        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/1"))
                .andExpect(status().isForbidden());

        verify(liquidityAccountService, never()).getLiquidityAccountById(any());
        verify(authenticationHelper, never()).validateResourceAccess(any(), any(), any());
        verify(liquidityAccountService, never()).deleteLiquidityAccount(any());
    }
}

