package com.giuseppesica.maney.portfolio;

import com.giuseppesica.maney.account.liquidityaccount.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.liquidityaccount.service.LiquidityAccountService;
import com.giuseppesica.maney.config.SecurityConfig;
import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.controller.PortfolioController;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.security.AuthenticationHelper;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.utils.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
@Import(SecurityConfig.class)
public class
PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IlliquidAssetService illiquidAssetService;

    @MockitoBean
    private LiquidityAccountService liquidityAccountService;

    @MockitoBean
    private AuthenticationHelper authenticationHelper;

    private Portfolio portfolio;

    @BeforeEach
    public void setUp() {
        portfolio = new Portfolio();
        portfolio.setId(1L);
    }

    // ==================== GET PORTFOLIO TESTS ====================

    @Test
    @WithMockUser(username = "john@example.com")
    public void testGetPortfolio_Success_ReturnsPortfolioWithAssets() throws Exception {
        // Given
        List<IlliquidAssetDto> illiquidAssets = new ArrayList<>();
        IlliquidAssetDto asset1 = new IlliquidAssetDto();
        asset1.setId(1L);
        asset1.setName("Vintage Car");
        asset1.setDescription("1967 Mustang");
        asset1.setEstimatedValue(75000.0f);
        illiquidAssets.add(asset1);

        List<LiquidityAccountDto> liquidityAccounts = new ArrayList<>();
        LiquidityAccountDto account1 = new LiquidityAccountDto();
        account1.setName("Checking Account");
        account1.setInstitution("Bank of America");
        account1.setCurrency(Currency.USD);
        account1.setBalance(new BigDecimal("5000.00"));
        account1.setPortfolioId(1L);
        liquidityAccounts.add(account1);

        when(authenticationHelper.getAuthenticatedUserPortfolio(any())).thenReturn(portfolio);
        when(illiquidAssetService.getIlliquidAssets(1L)).thenReturn(illiquidAssets);
        when(liquidityAccountService.getLiquidityAccounts(1L)).thenReturn(liquidityAccounts);

        // When & Then
        mockMvc.perform(get("/user/portfolio")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.illiquidAssets").isArray())
                .andExpect(jsonPath("$.illiquidAssets[0].id").value(1))
                .andExpect(jsonPath("$.illiquidAssets[0].name").value("Vintage Car"))
                .andExpect(jsonPath("$.liquidityAccounts").isArray())
                .andExpect(jsonPath("$.liquidityAccounts[0].name").value("Checking Account"))
                .andExpect(jsonPath("$.liquidityAccounts[0].institution").value("Bank of America"));

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any());
        verify(illiquidAssetService, times(1)).getIlliquidAssets(1L);
        verify(liquidityAccountService, times(1)).getLiquidityAccounts(1L);
    }

    @Test
    @WithMockUser(username = "john@example.com")
    public void testGetPortfolio_EmptyPortfolio_ReturnsEmptyLists() throws Exception {
        // Given
        when(authenticationHelper.getAuthenticatedUserPortfolio(any())).thenReturn(portfolio);
        when(illiquidAssetService.getIlliquidAssets(1L)).thenReturn(new ArrayList<>());
        when(liquidityAccountService.getLiquidityAccounts(1L)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/user/portfolio")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.illiquidAssets").isArray())
                .andExpect(jsonPath("$.illiquidAssets").isEmpty())
                .andExpect(jsonPath("$.liquidityAccounts").isArray())
                .andExpect(jsonPath("$.liquidityAccounts").isEmpty());

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any());
        verify(illiquidAssetService, times(1)).getIlliquidAssets(1L);
        verify(liquidityAccountService, times(1)).getLiquidityAccounts(1L);
    }

    @Test
    @WithMockUser(username = "john@example.com")
    public void testGetPortfolio_PortfolioNotFound_ReturnsNotFound() throws Exception {
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
        verify(liquidityAccountService, never()).getLiquidityAccounts(any());
    }

    @Test
    public void testGetPortfolio_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/user/portfolio"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        verify(authenticationHelper, never()).getAuthenticatedUserPortfolio(any());
        verify(illiquidAssetService, never()).getIlliquidAssets(any());
        verify(liquidityAccountService, never()).getLiquidityAccounts(any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    public void testGetPortfolio_WithMultipleAssets_ReturnsAll() throws Exception {
        // Given
        List<IlliquidAssetDto> illiquidAssets = new ArrayList<>();
        IlliquidAssetDto asset1 = new IlliquidAssetDto();
        asset1.setId(1L);
        asset1.setName("Vintage Car");
        asset1.setEstimatedValue(75000.0f);
        IlliquidAssetDto asset2 = new IlliquidAssetDto();
        asset2.setId(2L);
        asset2.setName("Artwork");
        asset2.setEstimatedValue(50000.0f);
        illiquidAssets.add(asset1);
        illiquidAssets.add(asset2);

        List<LiquidityAccountDto> liquidityAccounts = new ArrayList<>();
        LiquidityAccountDto account1 = new LiquidityAccountDto();
        account1.setName("Checking");
        account1.setInstitution("Bank A");
        account1.setCurrency(Currency.USD);
        account1.setBalance(new BigDecimal("5000.00"));
        account1.setPortfolioId(1L);
        LiquidityAccountDto account2 = new LiquidityAccountDto();
        account2.setName("Savings");
        account2.setInstitution("Bank B");
        account2.setCurrency(Currency.EUR);
        account2.setBalance(new BigDecimal("20000.00"));
        account2.setPortfolioId(1L);
        liquidityAccounts.add(account1);
        liquidityAccounts.add(account2);

        when(authenticationHelper.getAuthenticatedUserPortfolio(any())).thenReturn(portfolio);
        when(illiquidAssetService.getIlliquidAssets(1L)).thenReturn(illiquidAssets);
        when(liquidityAccountService.getLiquidityAccounts(1L)).thenReturn(liquidityAccounts);

        // When & Then
        mockMvc.perform(get("/user/portfolio")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.illiquidAssets").isArray())
                .andExpect(jsonPath("$.illiquidAssets.length()").value(2))
                .andExpect(jsonPath("$.illiquidAssets[0].name").value("Vintage Car"))
                .andExpect(jsonPath("$.illiquidAssets[1].name").value("Artwork"))
                .andExpect(jsonPath("$.liquidityAccounts").isArray())
                .andExpect(jsonPath("$.liquidityAccounts.length()").value(2))
                .andExpect(jsonPath("$.liquidityAccounts[0].name").value("Checking"))
                .andExpect(jsonPath("$.liquidityAccounts[1].name").value("Savings"));

        verify(authenticationHelper, times(1)).getAuthenticatedUserPortfolio(any());
        verify(illiquidAssetService, times(1)).getIlliquidAssets(1L);
        verify(liquidityAccountService, times(1)).getLiquidityAccounts(1L);
    }
}

