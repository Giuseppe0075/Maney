package com.giuseppesica.maney.account;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.model.LiquidityAccountRepository;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.model.PortfolioRepository;
import com.giuseppesica.maney.utils.CashMovementType;
import com.giuseppesica.maney.utils.Currency;
import com.giuseppesica.maney.security.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LiquidityAccountService.
 * Tests all business logic methods including CRUD operations and balance updates.
 */
public class LiquidityAccountServiceTest {

    @Mock
    private LiquidityAccountRepository liquidityAccountRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private LiquidityAccountService liquidityAccountService;

    private Portfolio portfolio;
    private LiquidityAccount liquidityAccount;
    private LiquidityAccountDto liquidityAccountDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

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

        // Setup DTO
        liquidityAccountDto = new LiquidityAccountDto();
        liquidityAccountDto.setName("Conto Corrente");
        liquidityAccountDto.setInstitution("Intesa Sanpaolo");
        liquidityAccountDto.setBalance(new BigDecimal("1000.00"));
        liquidityAccountDto.setCurrency(Currency.EUR);
        liquidityAccountDto.setOpenedAt(Instant.parse("2024-01-01T10:00:00Z"));
        liquidityAccountDto.setNote("Main account");
        liquidityAccountDto.setPortfolioId(1L);
    }

    // ==================== SAVE LIQUIDITY ACCOUNT TESTS ====================

    @Test
    public void testSaveLiquidityAccount_Success_ReturnsAccount() {
        // Given
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(liquidityAccountRepository.save(any(LiquidityAccount.class))).thenReturn(liquidityAccount);

        // When
        LiquidityAccount result = liquidityAccountService.saveLiquidityAccount(liquidityAccount);

        // Then
        assertNotNull(result);
        assertEquals("Conto Corrente", result.getName());
        assertEquals("Intesa Sanpaolo", result.getInstitution());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        assertEquals(Currency.EUR, result.getCurrency());
        assertEquals(portfolio, result.getPortfolio());

        verify(portfolioRepository, times(1)).findById(1L);
        verify(liquidityAccountRepository, times(1)).save(any(LiquidityAccount.class));
    }

    @Test
    public void testSaveLiquidityAccount_PortfolioNotFound_ThrowsException() {
        // Given
        Portfolio missingPortfolio = new Portfolio();
        missingPortfolio.setId(999L);
        LiquidityAccount accountForTest = new LiquidityAccount();
        accountForTest.setName(liquidityAccount.getName());
        accountForTest.setInstitution(liquidityAccount.getInstitution());
        accountForTest.setBalance(liquidityAccount.getBalance());
        accountForTest.setCurrency(liquidityAccount.getCurrency());
        accountForTest.setOpenedAt(liquidityAccount.getOpenedAt());
        accountForTest.setNote(liquidityAccount.getNote());
        accountForTest.setPortfolio(missingPortfolio);

        when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> liquidityAccountService.saveLiquidityAccount(accountForTest));
        assertEquals("Portfolio not found", exception.getMessage());

        verify(portfolioRepository, times(1)).findById(999L);
        verify(liquidityAccountRepository, never()).save(any());
    }

    @Test
    public void testSaveLiquidityAccount_WithNullFields_SavesSuccessfully() {
        // Given - Optional fields are null
        liquidityAccountDto.setNote(null);
        liquidityAccountDto.setClosedAt(null);

        LiquidityAccount accountWithNullFields = new LiquidityAccount();
        accountWithNullFields.setName("Conto Corrente");
        accountWithNullFields.setInstitution("Intesa Sanpaolo");
        accountWithNullFields.setBalance(new BigDecimal("1000.00"));
        accountWithNullFields.setCurrency(Currency.EUR);
        accountWithNullFields.setPortfolio(portfolio);
        accountWithNullFields.setNote(null);
        accountWithNullFields.setClosedAt(null);

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(liquidityAccountRepository.save(any(LiquidityAccount.class))).thenReturn(accountWithNullFields);

        // When
        LiquidityAccount result = liquidityAccountService.saveLiquidityAccount(liquidityAccount);

        // Then
        assertNotNull(result);
        assertNull(result.getNote());
        assertNull(result.getClosedAt());

        verify(portfolioRepository, times(1)).findById(1L);
        verify(liquidityAccountRepository, times(1)).save(any(LiquidityAccount.class));
    }

    // ==================== GET LIQUIDITY ACCOUNTS TESTS ====================

    @Test
    public void testGetLiquidityAccounts_Success_ReturnsListOfDtos() {
        // Given
        LiquidityAccount account2 = new LiquidityAccount();
        account2.setName("Savings Account");
        account2.setInstitution("UniCredit");
        account2.setBalance(new BigDecimal("5000.00"));
        account2.setCurrency(Currency.EUR);
        account2.setPortfolio(portfolio);

        when(liquidityAccountRepository.findByPortfolioId(1L))
                .thenReturn(Arrays.asList(liquidityAccount, account2));

        // When
        List<LiquidityAccountDto> result = liquidityAccountService.getLiquidityAccounts(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Conto Corrente", result.get(0).getName());
        assertEquals("Savings Account", result.get(1).getName());

        verify(liquidityAccountRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    public void testGetLiquidityAccounts_EmptyList_ReturnsEmptyList() {
        // Given
        when(liquidityAccountRepository.findByPortfolioId(1L)).thenReturn(List.of());

        // When
        List<LiquidityAccountDto> result = liquidityAccountService.getLiquidityAccounts(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(liquidityAccountRepository, times(1)).findByPortfolioId(1L);
    }

    // ==================== GET LIQUIDITY ACCOUNT BY ID TESTS ====================

    @Test
    public void testGetLiquidityAccountById_Success_ReturnsAccount() {
        // Given
        when(liquidityAccountRepository.findById(1L)).thenReturn(Optional.of(liquidityAccount));

        // When
        Optional<LiquidityAccount> result = liquidityAccountService.getLiquidityAccountById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Conto Corrente", result.get().getName());
        assertEquals(new BigDecimal("1000.00"), result.get().getBalance());

        verify(liquidityAccountRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetLiquidityAccountById_NotFound_ReturnsEmpty() {
        // Given
        when(liquidityAccountRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<LiquidityAccount> result = liquidityAccountService.getLiquidityAccountById(999L);

        // Then
        assertFalse(result.isPresent());

        verify(liquidityAccountRepository, times(1)).findById(999L);
    }

    // ==================== UPDATE LIQUIDITY ACCOUNT TESTS ====================

    @Test
    public void testUpdateLiquidityAccount_Success_ReturnsUpdatedAccount() {
        // Given
        LiquidityAccountDto updateDto = new LiquidityAccountDto();
        updateDto.setName("Updated Account");
        updateDto.setInstitution("UniCredit");
        updateDto.setBalance(new BigDecimal("2000.00"));
        updateDto.setCurrency(Currency.USD);
        updateDto.setOpenedAt(Instant.parse("2024-02-01T10:00:00Z"));
        updateDto.setNote("Updated note");
        updateDto.setPortfolioId(1L);

        LiquidityAccount updatedAccount = new LiquidityAccount();
        updatedAccount.setName("Updated Account");
        updatedAccount.setInstitution("UniCredit");
        updatedAccount.setBalance(new BigDecimal("2000.00"));
        updatedAccount.setCurrency(Currency.USD);
        updatedAccount.setNote("Updated note");
        updatedAccount.setPortfolio(portfolio);

        when(liquidityAccountRepository.findById(1L)).thenReturn(Optional.of(liquidityAccount));
        when(liquidityAccountRepository.save(any(LiquidityAccount.class))).thenReturn(updatedAccount);

        // When
        LiquidityAccount result = liquidityAccountService.updateLiquidityAccount(1L, updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Account", result.getName());
        assertEquals("UniCredit", result.getInstitution());
        assertEquals(new BigDecimal("2000.00"), result.getBalance());
        assertEquals(Currency.USD, result.getCurrency());
        assertEquals("Updated note", result.getNote());

        verify(liquidityAccountRepository, times(1)).findById(1L);
        verify(liquidityAccountRepository, times(1)).save(any(LiquidityAccount.class));
    }

    @Test
    public void testUpdateLiquidityAccount_NotFound_ThrowsException() {
        // Given
        when(liquidityAccountRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> liquidityAccountService.updateLiquidityAccount(999L, liquidityAccountDto));
        assertEquals("Liquidity account not found", exception.getMessage());

        verify(liquidityAccountRepository, times(1)).findById(999L);
        verify(liquidityAccountRepository, never()).save(any());
    }

    @Test
    public void testUpdateLiquidityAccount_PartialUpdate_OnlyChangesSpecifiedFields() {
        // Given - Only updating name and balance
        LiquidityAccountDto partialUpdateDto = new LiquidityAccountDto();
        partialUpdateDto.setName("Partially Updated");
        partialUpdateDto.setInstitution("Intesa Sanpaolo"); // Kept same
        partialUpdateDto.setBalance(new BigDecimal("1500.00"));
        partialUpdateDto.setCurrency(Currency.EUR);
        partialUpdateDto.setOpenedAt(liquidityAccount.getOpenedAt());
        partialUpdateDto.setNote(liquidityAccount.getNote());

        when(liquidityAccountRepository.findById(1L)).thenReturn(Optional.of(liquidityAccount));
        when(liquidityAccountRepository.save(any(LiquidityAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        LiquidityAccount result = liquidityAccountService.updateLiquidityAccount(1L, partialUpdateDto);

        // Then
        assertNotNull(result);
        assertEquals("Partially Updated", result.getName());
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        assertEquals("Intesa Sanpaolo", result.getInstitution());

        verify(liquidityAccountRepository, times(1)).findById(1L);
        verify(liquidityAccountRepository, times(1)).save(any(LiquidityAccount.class));
    }

    // ==================== DELETE LIQUIDITY ACCOUNT TESTS ====================

    @Test
    public void testDeleteLiquidityAccount_Success_DeletesAccount() {
        // Given
        when(liquidityAccountRepository.existsById(1L)).thenReturn(true);
        doNothing().when(liquidityAccountRepository).deleteById(1L);

        // When
        liquidityAccountService.deleteLiquidityAccount(1L);

        // Then
        verify(liquidityAccountRepository, times(1)).existsById(1L);
        verify(liquidityAccountRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteLiquidityAccount_NotFound_ThrowsException() {
        // Given
        when(liquidityAccountRepository.existsById(999L)).thenReturn(false);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> liquidityAccountService.deleteLiquidityAccount(999L));
        assertEquals("Liquidity account not found", exception.getMessage());

        verify(liquidityAccountRepository, times(1)).existsById(999L);
        verify(liquidityAccountRepository, never()).deleteById(any());
    }

    // ==================== GET BY PORTFOLIO AND NAME TESTS ====================

    @Test
    public void testGetLiquidityAccountByPortfolioIdAndName_Success_ReturnsAccount() {
        // Given
        when(liquidityAccountRepository.findByPortfolioId(1L))
                .thenReturn(Collections.singletonList(liquidityAccount));

        // When
        Optional<LiquidityAccount> result = liquidityAccountService
                .getLiquidityAccountByPortfolioIdAndName(1L, "Conto Corrente");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Conto Corrente", result.get().getName());
        assertEquals(portfolio.getId(), result.get().getPortfolio().getId());

        verify(liquidityAccountRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    public void testGetLiquidityAccountByPortfolioIdAndName_NotFound_ReturnsEmpty() {
        // Given
        when(liquidityAccountRepository.findByPortfolioId(1L))
                .thenReturn(Collections.singletonList(liquidityAccount));

        // When
        Optional<LiquidityAccount> result = liquidityAccountService
                .getLiquidityAccountByPortfolioIdAndName(1L, "Non-Existent Account");

        // Then
        assertFalse(result.isPresent());

        verify(liquidityAccountRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    public void testGetLiquidityAccountByPortfolioIdAndName_MultipleAccounts_ReturnsCorrectOne() {
        // Given
        LiquidityAccount account2 = new LiquidityAccount();
        account2.setName("Savings Account");
        account2.setInstitution("UniCredit");
        account2.setBalance(new BigDecimal("5000.00"));
        account2.setCurrency(Currency.EUR);
        account2.setPortfolio(portfolio);

        when(liquidityAccountRepository.findByPortfolioId(1L))
                .thenReturn(Arrays.asList(liquidityAccount, account2));

        // When
        Optional<LiquidityAccount> result = liquidityAccountService
                .getLiquidityAccountByPortfolioIdAndName(1L, "Savings Account");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Savings Account", result.get().getName());

        verify(liquidityAccountRepository, times(1)).findByPortfolioId(1L);
    }

    // ==================== UPDATE LIQUIDITY ACCOUNT BALANCE TESTS ====================

    @Test
    public void testUpdateLiquidityAccount_IncomeType_IncreasesBalance() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal incomeAmount = new BigDecimal("500.00");
        liquidityAccount.setBalance(initialBalance);

        when(liquidityAccountRepository.save(any(LiquidityAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        liquidityAccountService.updateLiquidityAccount(liquidityAccount, incomeAmount, CashMovementType.INCOME);

        // Then
        assertEquals(new BigDecimal("1500.00"), liquidityAccount.getBalance());
        verify(liquidityAccountRepository, times(1)).save(liquidityAccount);
    }

    @Test
    public void testUpdateLiquidityAccount_OutcomeType_DecreasesBalance() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal outcomeAmount = new BigDecimal("300.00");
        liquidityAccount.setBalance(initialBalance);

        when(liquidityAccountRepository.save(any(LiquidityAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        liquidityAccountService.updateLiquidityAccount(liquidityAccount, outcomeAmount, CashMovementType.OUTCOME);

        // Then
        assertEquals(new BigDecimal("700.00"), liquidityAccount.getBalance());
        verify(liquidityAccountRepository, times(1)).save(liquidityAccount);
    }

    @Test
    public void testUpdateLiquidityAccount_InvalidType_ThrowsException() {
        // Given
        BigDecimal amount = new BigDecimal("500.00");

        // When & Then - Using null as invalid type
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> liquidityAccountService.updateLiquidityAccount(liquidityAccount, amount, null));
        assertEquals("Invalid Cash Movement Type", exception.getMessage());

        verify(liquidityAccountRepository, never()).save(any());
    }

    @Test
    public void testUpdateLiquidityAccount_LargeIncome_UpdatesBalanceCorrectly() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal largeIncome = new BigDecimal("999999.99");
        liquidityAccount.setBalance(initialBalance);

        when(liquidityAccountRepository.save(any(LiquidityAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        liquidityAccountService.updateLiquidityAccount(liquidityAccount, largeIncome, CashMovementType.INCOME);

        // Then
        assertEquals(new BigDecimal("1000999.99"), liquidityAccount.getBalance());
        verify(liquidityAccountRepository, times(1)).save(liquidityAccount);
    }

    @Test
    public void testUpdateLiquidityAccount_OutcomeExceedsBalance_AllowsNegativeBalance() {
        // Given - Assuming the system allows negative balances
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal largeOutcome = new BigDecimal("500.00");
        liquidityAccount.setBalance(initialBalance);

        when(liquidityAccountRepository.save(any(LiquidityAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        liquidityAccountService.updateLiquidityAccount(liquidityAccount, largeOutcome, CashMovementType.OUTCOME);

        // Then
        assertEquals(new BigDecimal("-400.00"), liquidityAccount.getBalance());
        verify(liquidityAccountRepository, times(1)).save(liquidityAccount);
    }

    @Test
    public void testUpdateLiquidityAccount_ZeroAmount_BalanceUnchanged() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal zeroAmount = BigDecimal.ZERO;
        liquidityAccount.setBalance(initialBalance);

        when(liquidityAccountRepository.save(any(LiquidityAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        liquidityAccountService.updateLiquidityAccount(liquidityAccount, zeroAmount, CashMovementType.INCOME);

        // Then
        assertEquals(initialBalance, liquidityAccount.getBalance());
        verify(liquidityAccountRepository, times(1)).save(liquidityAccount);
    }

    @Test
    public void testUpdateLiquidityAccount_DecimalPrecision_MaintainsPrecision() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.123");
        BigDecimal preciseAmount = new BigDecimal("0.456");
        liquidityAccount.setBalance(initialBalance);

        when(liquidityAccountRepository.save(any(LiquidityAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        liquidityAccountService.updateLiquidityAccount(liquidityAccount, preciseAmount, CashMovementType.INCOME);

        // Then
        assertEquals(new BigDecimal("1000.579"), liquidityAccount.getBalance());
        verify(liquidityAccountRepository, times(1)).save(liquidityAccount);
    }
}
