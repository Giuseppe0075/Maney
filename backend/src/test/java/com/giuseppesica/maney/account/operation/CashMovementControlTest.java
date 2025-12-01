package com.giuseppesica.maney.account.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.cashmovement.model.CashMovement;
import com.giuseppesica.maney.account.operation.cashmovement.control.CashMovementControl;
import com.giuseppesica.maney.account.operation.cashmovement.model.CashMovementDto;
import com.giuseppesica.maney.account.operation.cashmovement.service.CashMovementService;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.category.service.CategoryService;
import com.giuseppesica.maney.config.SecurityConfig;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import com.giuseppesica.maney.utils.CashMovementType;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CashMovementControl.
 * Tests all CRUD operations and validates authentication, authorization, and error handling.
 */
@WebMvcTest(CashMovementControl.class)
@Import(SecurityConfig.class)
public class CashMovementControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CashMovementService cashMovementService;

    @MockitoBean
    private LiquidityAccountService liquidityAccountService;

    @MockitoBean
    private CategoryService categoryService;

    private User user;
    private LiquidityAccount liquidityAccount;
    private Category category;
    private CashMovement cashMovement;
    private CashMovementDto cashMovementDto;

    @BeforeEach
    public void setUp() {
        // Setup portfolio
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        // Setup user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPortfolio(portfolio);

        // Setup liquidity account
        liquidityAccount = new LiquidityAccount();
        liquidityAccount.setId(1L);
        liquidityAccount.setName("Conto Corrente Intesa");
        liquidityAccount.setInstitution("Intesa Sanpaolo");
        liquidityAccount.setBalance(new BigDecimal("1000.00"));
        liquidityAccount.setCurrency(Currency.EUR);
        liquidityAccount.setPortfolio(portfolio);

        // Setup category
        category = new Category();
        category.setId(1L);
        category.setName("Spesa");
        category.setUser(user);

        // Setup cash movement entity
        cashMovement = new CashMovement();
        cashMovement.setId(1L);
        cashMovement.setDate(Instant.parse("2025-01-01T10:00:00Z"));
        cashMovement.setNote("Stipendio gennaio");
        cashMovement.setAmount(new BigDecimal("1500.00"));
        cashMovement.setType(CashMovementType.INCOME);
        cashMovement.setLiquidityAccount(liquidityAccount);
        cashMovement.setCategory(category);

        // Setup DTO for requests
        cashMovementDto = new CashMovementDto();
        cashMovementDto.setDate(Instant.parse("2025-01-01T10:00:00Z"));
        cashMovementDto.setNote("Stipendio gennaio");
        cashMovementDto.setAmount(new BigDecimal("1500.00"));
        cashMovementDto.setType(CashMovementType.INCOME);
        cashMovementDto.setLiquidityAccountName("Conto Corrente Intesa");
        cashMovementDto.setCategoryId(1L);
    }

    // ==================== GET ALL CASH MOVEMENTS TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetCashMovements_Success_ReturnsList() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementsByUserId(user))
                .thenReturn(List.of(cashMovement));

        // When & Then
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/cash-movements")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].note").value("Stipendio gennaio"))
                .andExpect(jsonPath("$[0].amount").value(1500.00))
                .andExpect(jsonPath("$[0].type").value("INCOME"))
                .andExpect(jsonPath("$[0].liquidityAccountName").value("Conto Corrente Intesa"));

        verify(userService, times(1)).UserFromAuthentication(any());
        verify(cashMovementService, times(1)).getCashMovementsByUserId(user);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetCashMovements_EmptyList_ReturnsEmptyArray() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementsByUserId(user))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/cash-movements")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(cashMovementService, times(1)).getCashMovementsByUserId(user);
    }

    @Test
    public void testGetCashMovements_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/cash-movements"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        verify(userService, never()).UserFromAuthentication(any());
        verify(cashMovementService, never()).getCashMovementsByUserId(any());
    }

    // ==================== GET CASH MOVEMENT BY ID TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetCashMovementById_Success_ReturnsMovement() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementByIdAndUserId(1L, user))
                .thenReturn(Optional.of(cashMovement));

        // When & Then
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/cash-movements/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Stipendio gennaio"))
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.type").value("INCOME"));

        verify(userService, times(1)).UserFromAuthentication(any());
        verify(cashMovementService, times(1)).getCashMovementByIdAndUserId(1L, user);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetCashMovementById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementByIdAndUserId(999L, user))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/cash-movements/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cash Movement Not Found"));

        verify(cashMovementService, times(1)).getCashMovementByIdAndUserId(999L, user);
    }

    @Test
    public void testGetCashMovementById_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/cash-movements/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        verify(userService, never()).UserFromAuthentication(any());
        verify(cashMovementService, never()).getCashMovementByIdAndUserId(any(), any());
    }

    // ==================== CREATE CASH MOVEMENT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateCashMovement_Success_ReturnsCreated() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(liquidityAccountService.getLiquidityAccountByPortfolioIdAndName(1L, "Conto Corrente Intesa"))
                .thenReturn(Optional.of(liquidityAccount));
        when(categoryService.findByUserAndId(1L, 1L))
                .thenReturn(Optional.of(category));
        when(cashMovementService.saveCashMovement(any(CashMovement.class)))
                .thenReturn(cashMovement);
        doNothing().when(liquidityAccountService).updateLiquidityAccount(
                any(LiquidityAccount.class),
                any(BigDecimal.class),
                any(CashMovementType.class)
        );

        // When & Then
        mockMvc.perform(post("/user/portfolio/liquidity-accounts/cash-movements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashMovementDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Stipendio gennaio"))
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.liquidityAccountName").value("Conto Corrente Intesa"));

        verify(liquidityAccountService, times(1))
                .getLiquidityAccountByPortfolioIdAndName(1L, "Conto Corrente Intesa");
        verify(categoryService, times(1)).findByUserAndId(1L, 1L);
        verify(cashMovementService, times(1)).saveCashMovement(any(CashMovement.class));
        verify(liquidityAccountService, times(1)).updateLiquidityAccount(
                eq(liquidityAccount),
                eq(new BigDecimal("1500.00")),
                eq(CashMovementType.INCOME)
        );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateCashMovement_LiquidityAccountNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(liquidityAccountService.getLiquidityAccountByPortfolioIdAndName(1L, "Conto Corrente Intesa"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/user/portfolio/liquidity-accounts/cash-movements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashMovementDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Liquidity Account Not Found"));

        verify(liquidityAccountService, times(1))
                .getLiquidityAccountByPortfolioIdAndName(1L, "Conto Corrente Intesa");
        verify(categoryService, never()).findByUserAndId(any(), any());
        verify(cashMovementService, never()).saveCashMovement(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateCashMovement_CategoryNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(liquidityAccountService.getLiquidityAccountByPortfolioIdAndName(1L, "Conto Corrente Intesa"))
                .thenReturn(Optional.of(liquidityAccount));
        when(categoryService.findByUserAndId(1L, 1L))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/user/portfolio/liquidity-accounts/cash-movements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashMovementDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Category Not Found"));

        verify(categoryService, times(1)).findByUserAndId(1L, 1L);
        verify(cashMovementService, never()).saveCashMovement(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateCashMovement_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        // Given - Missing required field 'amount'
        cashMovementDto.setAmount(null);

        // When & Then
        mockMvc.perform(post("/user/portfolio/liquidity-accounts/cash-movements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashMovementDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).UserFromAuthentication(any());
        verify(cashMovementService, never()).saveCashMovement(any());
    }

    @Test
    public void testCreateCashMovement_Unauthenticated_ReturnsForbidden() throws Exception {
        // When & Then - No authentication and no CSRF token
        mockMvc.perform(post("/user/portfolio/liquidity-accounts/cash-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashMovementDto)))
                .andExpect(status().isForbidden());

        verify(userService, never()).UserFromAuthentication(any());
        verify(cashMovementService, never()).saveCashMovement(any());
    }

    // ==================== UPDATE CASH MOVEMENT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateCashMovement_Success_ReturnsUpdated() throws Exception {
        // Given
        CashMovementDto updateDto = new CashMovementDto();
        updateDto.setDate(Instant.parse("2025-01-02T10:00:00Z"));
        updateDto.setNote("Stipendio aggiornato");
        updateDto.setAmount(new BigDecimal("2000.00"));
        updateDto.setType(CashMovementType.INCOME);
        updateDto.setLiquidityAccountName("Conto Corrente Intesa");
        updateDto.setCategoryId(1L);

        CashMovement updatedMovement = new CashMovement();
        updatedMovement.setId(1L);
        updatedMovement.setDate(Instant.parse("2025-01-02T10:00:00Z"));
        updatedMovement.setNote("Stipendio aggiornato");
        updatedMovement.setAmount(new BigDecimal("2000.00"));
        updatedMovement.setType(CashMovementType.INCOME);
        updatedMovement.setLiquidityAccount(liquidityAccount);
        updatedMovement.setCategory(category);

        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementByIdAndUserId(1L, user))
                .thenReturn(Optional.of(cashMovement));
        when(cashMovementService.saveCashMovement(any(CashMovement.class)))
                .thenReturn(updatedMovement);
        doNothing().when(liquidityAccountService).updateLiquidityAccount(
                any(LiquidityAccount.class),
                any(BigDecimal.class),
                any(CashMovementType.class)
        );

        // When & Then
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/cash-movements/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Stipendio aggiornato"))
                .andExpect(jsonPath("$.amount").value(2000.00));

        verify(cashMovementService, times(1)).getCashMovementByIdAndUserId(1L, user);
        verify(cashMovementService, times(1)).saveCashMovement(any(CashMovement.class));
        // Verify balance update: revert old + apply new
        verify(liquidityAccountService, times(2)).updateLiquidityAccount(
                any(LiquidityAccount.class),
                any(BigDecimal.class),
                any(CashMovementType.class)
        );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateCashMovement_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementByIdAndUserId(999L, user))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/cash-movements/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashMovementDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cash Movement Not Found"));

        verify(cashMovementService, times(1)).getCashMovementByIdAndUserId(999L, user);
        verify(cashMovementService, never()).saveCashMovement(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateCashMovement_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        // Given - Missing required field 'date'
        cashMovementDto.setDate(null);

        // When & Then
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/cash-movements/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashMovementDto)))
                .andExpect(status().isBadRequest());

        verify(cashMovementService, never()).getCashMovementByIdAndUserId(any(), any());
        verify(cashMovementService, never()).saveCashMovement(any());
    }

    @Test
    public void testUpdateCashMovement_Unauthenticated_ReturnsForbidden() throws Exception {
        // When & Then - No authentication and no CSRF token
        mockMvc.perform(put("/user/portfolio/liquidity-accounts/cash-movements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashMovementDto)))
                .andExpect(status().isForbidden());

        verify(userService, never()).UserFromAuthentication(any());
        verify(cashMovementService, never()).saveCashMovement(any());
    }

    // ==================== DELETE CASH MOVEMENT TESTS ====================

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteCashMovement_Success_ReturnsNoContent() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementByIdAndUserId(1L, user))
                .thenReturn(Optional.of(cashMovement));
        doNothing().when(liquidityAccountService).updateLiquidityAccount(
                any(LiquidityAccount.class),
                any(BigDecimal.class),
                any(CashMovementType.class)
        );
        doNothing().when(cashMovementService).deleteCashMovement(cashMovement);

        // When & Then
        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/cash-movements/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cashMovementService, times(1)).getCashMovementByIdAndUserId(1L, user);
        verify(liquidityAccountService, times(1)).updateLiquidityAccount(
                eq(liquidityAccount),
                eq(new BigDecimal("1500.00")),
                eq(CashMovementType.OUTCOME) // Reverting INCOME
        );
        verify(cashMovementService, times(1)).deleteCashMovement(cashMovement);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteCashMovement_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementByIdAndUserId(999L, user))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/cash-movements/999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cash Movement Not Found"));

        verify(cashMovementService, times(1)).getCashMovementByIdAndUserId(999L, user);
        verify(cashMovementService, never()).deleteCashMovement(any());
    }

    @Test
    public void testDeleteCashMovement_Unauthenticated_ReturnsForbidden() throws Exception {
        // When & Then - No authentication and no CSRF token
        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/cash-movements/1"))
                .andExpect(status().isForbidden());

        verify(userService, never()).UserFromAuthentication(any());
        verify(cashMovementService, never()).deleteCashMovement(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteCashMovement_OutcomeType_RevertsCorrectly() throws Exception {
        // Given - Outcome movement should be reverted as INCOME
        CashMovement outcomeCashMovement = new CashMovement();
        outcomeCashMovement.setId(2L);
        outcomeCashMovement.setDate(Instant.parse("2025-01-01T10:00:00Z"));
        outcomeCashMovement.setNote("Spesa supermercato");
        outcomeCashMovement.setAmount(new BigDecimal("100.00"));
        outcomeCashMovement.setType(CashMovementType.OUTCOME);
        outcomeCashMovement.setLiquidityAccount(liquidityAccount);
        outcomeCashMovement.setCategory(category);

        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(cashMovementService.getCashMovementByIdAndUserId(2L, user))
                .thenReturn(Optional.of(outcomeCashMovement));
        doNothing().when(liquidityAccountService).updateLiquidityAccount(
                any(LiquidityAccount.class),
                any(BigDecimal.class),
                any(CashMovementType.class)
        );
        doNothing().when(cashMovementService).deleteCashMovement(outcomeCashMovement);

        // When & Then
        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/cash-movements/2")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(liquidityAccountService, times(1)).updateLiquidityAccount(
                eq(liquidityAccount),
                eq(new BigDecimal("100.00")),
                eq(CashMovementType.INCOME) // Reverting OUTCOME
        );
        verify(cashMovementService, times(1)).deleteCashMovement(outcomeCashMovement);
    }
}

