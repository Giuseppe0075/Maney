package com.giuseppesica.maney.account.operations.cashmovement;

import com.giuseppesica.maney.account.liquidityaccount.model.LiquidityAccount;
import com.giuseppesica.maney.account.operations.cashmovement.model.CashMovement;
import com.giuseppesica.maney.account.operations.cashmovement.model.CashMovementRepository;
import com.giuseppesica.maney.account.operations.cashmovement.service.CashMovementService;
import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.utils.CashMovementType;
import com.giuseppesica.maney.utils.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CashMovementService.
 * Tests all business logic methods for cash movement operations.
 */
public class CashMovementServiceTest {

    @Mock
    private CashMovementRepository cashMovementRepository;

    @InjectMocks
    private CashMovementService cashMovementService;

    private User user;
    private Portfolio portfolio;
    private LiquidityAccount liquidityAccount;
    private Category category;
    private CashMovement cashMovement;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup portfolio
        portfolio = new Portfolio();
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
        liquidityAccount.setName("Conto Corrente");
        liquidityAccount.setInstitution("Intesa Sanpaolo");
        liquidityAccount.setBalance(new BigDecimal("1000.00"));
        liquidityAccount.setCurrency(Currency.EUR);
        liquidityAccount.setPortfolio(portfolio);

        // Setup category
        category = new Category();
        category.setId(1L);
        category.setName("Spesa");
        category.setUser(user);

        // Setup cash movement
        cashMovement = new CashMovement();
        cashMovement.setId(1L);
        cashMovement.setDate(Instant.parse("2025-01-01T10:00:00Z"));
        cashMovement.setNote("Stipendio gennaio");
        cashMovement.setAmount(new BigDecimal("1500.00"));
        cashMovement.setType(CashMovementType.INCOME);
        cashMovement.setLiquidityAccount(liquidityAccount);
        cashMovement.setCategory(category);
    }

    // ==================== GET CASH MOVEMENTS BY USER ID TESTS ====================

    @Test
    public void testGetCashMovementsByUserId_Success_ReturnsList() {
        // Given
        CashMovement movement2 = new CashMovement();
        movement2.setId(2L);
        movement2.setDate(Instant.parse("2025-01-05T10:00:00Z"));
        movement2.setNote("Spesa supermercato");
        movement2.setAmount(new BigDecimal("100.00"));
        movement2.setType(CashMovementType.OUTCOME);
        movement2.setLiquidityAccount(liquidityAccount);
        movement2.setCategory(category);

        List<CashMovement> movements = List.of(cashMovement, movement2);
        when(cashMovementRepository.findByPortfolioId(1L)).thenReturn(movements);

        // When
        List<CashMovement> result = cashMovementService.getCashMovementsByUserId(user);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Stipendio gennaio", result.get(0).getNote());
        assertEquals("Spesa supermercato", result.get(1).getNote());
        verify(cashMovementRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    public void testGetCashMovementsByUserId_EmptyList_ReturnsEmptyList() {
        // Given
        when(cashMovementRepository.findByPortfolioId(1L)).thenReturn(new ArrayList<>());

        // When
        List<CashMovement> result = cashMovementService.getCashMovementsByUserId(user);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cashMovementRepository, times(1)).findByPortfolioId(1L);
    }

    // ==================== SAVE CASH MOVEMENT TESTS ====================

    @Test
    public void testSaveCashMovement_Success_ReturnsMovement() {
        // Given
        CashMovement newMovement = new CashMovement();
        newMovement.setDate(Instant.parse("2025-01-10T10:00:00Z"));
        newMovement.setNote("New movement");
        newMovement.setAmount(new BigDecimal("500.00"));
        newMovement.setType(CashMovementType.OUTCOME);
        newMovement.setLiquidityAccount(liquidityAccount);
        newMovement.setCategory(category);

        CashMovement savedMovement = new CashMovement();
        savedMovement.setId(3L);
        savedMovement.setDate(newMovement.getDate());
        savedMovement.setNote(newMovement.getNote());
        savedMovement.setAmount(newMovement.getAmount());
        savedMovement.setType(newMovement.getType());
        savedMovement.setLiquidityAccount(newMovement.getLiquidityAccount());
        savedMovement.setCategory(newMovement.getCategory());

        when(cashMovementRepository.save(newMovement)).thenReturn(savedMovement);

        // When
        CashMovement result = cashMovementService.saveCashMovement(newMovement);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New movement", result.getNote());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        assertEquals(CashMovementType.OUTCOME, result.getType());
        verify(cashMovementRepository, times(1)).save(newMovement);
    }

    @Test
    public void testSaveCashMovement_UpdateExisting_ReturnsUpdated() {
        // Given
        cashMovement.setNote("Updated note");
        cashMovement.setAmount(new BigDecimal("2000.00"));

        when(cashMovementRepository.save(cashMovement)).thenReturn(cashMovement);

        // When
        CashMovement result = cashMovementService.saveCashMovement(cashMovement);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated note", result.getNote());
        assertEquals(new BigDecimal("2000.00"), result.getAmount());
        verify(cashMovementRepository, times(1)).save(cashMovement);
    }

    @Test
    public void testSaveCashMovement_WithoutCategory_Success() {
        // Given
        CashMovement movementWithoutCategory = new CashMovement();
        movementWithoutCategory.setDate(Instant.parse("2025-01-10T10:00:00Z"));
        movementWithoutCategory.setNote("Uncategorized");
        movementWithoutCategory.setAmount(new BigDecimal("50.00"));
        movementWithoutCategory.setType(CashMovementType.OUTCOME);
        movementWithoutCategory.setLiquidityAccount(liquidityAccount);
        movementWithoutCategory.setCategory(null);

        CashMovement savedMovement = new CashMovement();
        savedMovement.setId(4L);
        savedMovement.setDate(movementWithoutCategory.getDate());
        savedMovement.setNote(movementWithoutCategory.getNote());
        savedMovement.setAmount(movementWithoutCategory.getAmount());
        savedMovement.setType(movementWithoutCategory.getType());
        savedMovement.setLiquidityAccount(movementWithoutCategory.getLiquidityAccount());
        savedMovement.setCategory(null);

        when(cashMovementRepository.save(movementWithoutCategory)).thenReturn(savedMovement);

        // When
        CashMovement result = cashMovementService.saveCashMovement(movementWithoutCategory);

        // Then
        assertNotNull(result);
        assertNull(result.getCategory());
        verify(cashMovementRepository, times(1)).save(movementWithoutCategory);
    }

    // ==================== GET CASH MOVEMENT BY ID AND USER ID TESTS ====================

    @Test
    public void testGetCashMovementByIdAndUserId_Success_ReturnsMovement() {
        // Given
        when(cashMovementRepository.findByIdAndPortfolioId(1L, 1L))
                .thenReturn(Optional.of(cashMovement));

        // When
        Optional<CashMovement> result = cashMovementService.getCashMovementByIdAndUserId(1L, user);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Stipendio gennaio", result.get().getNote());
        assertEquals(new BigDecimal("1500.00"), result.get().getAmount());
        verify(cashMovementRepository, times(1)).findByIdAndPortfolioId(1L, 1L);
    }

    @Test
    public void testGetCashMovementByIdAndUserId_NotFound_ReturnsEmpty() {
        // Given
        when(cashMovementRepository.findByIdAndPortfolioId(999L, 1L))
                .thenReturn(Optional.empty());

        // When
        Optional<CashMovement> result = cashMovementService.getCashMovementByIdAndUserId(999L, user);

        // Then
        assertFalse(result.isPresent());
        verify(cashMovementRepository, times(1)).findByIdAndPortfolioId(999L, 1L);
    }

    @Test
    public void testGetCashMovementByIdAndUserId_WrongPortfolio_ReturnsEmpty() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        Portfolio anotherPortfolio = new Portfolio();
        anotherPortfolio.setId(2L);
        anotherUser.setPortfolio(anotherPortfolio);

        when(cashMovementRepository.findByIdAndPortfolioId(1L, 2L))
                .thenReturn(Optional.empty());

        // When
        Optional<CashMovement> result = cashMovementService.getCashMovementByIdAndUserId(1L, anotherUser);

        // Then
        assertFalse(result.isPresent());
        verify(cashMovementRepository, times(1)).findByIdAndPortfolioId(1L, 2L);
    }

    // ==================== DELETE CASH MOVEMENT TESTS ====================

    @Test
    public void testDeleteCashMovement_Success_DeletesMovement() {
        // Given
        doNothing().when(cashMovementRepository).delete(cashMovement);

        // When
        cashMovementService.deleteCashMovement(cashMovement);

        // Then
        verify(cashMovementRepository, times(1)).delete(cashMovement);
    }

    @Test
    public void testDeleteCashMovement_IncomeMovement_Success() {
        // Given - Income movement
        doNothing().when(cashMovementRepository).delete(cashMovement);

        // When
        cashMovementService.deleteCashMovement(cashMovement);

        // Then
        verify(cashMovementRepository, times(1)).delete(cashMovement);
    }

    @Test
    public void testDeleteCashMovement_OutcomeMovement_Success() {
        // Given - Outcome movement
        CashMovement outcomeMovement = new CashMovement();
        outcomeMovement.setId(2L);
        outcomeMovement.setType(CashMovementType.OUTCOME);
        outcomeMovement.setAmount(new BigDecimal("100.00"));
        outcomeMovement.setLiquidityAccount(liquidityAccount);

        doNothing().when(cashMovementRepository).delete(outcomeMovement);

        // When
        cashMovementService.deleteCashMovement(outcomeMovement);

        // Then
        verify(cashMovementRepository, times(1)).delete(outcomeMovement);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    public void testGetCashMovementsByUserId_MultipleMovements_ReturnsAll() {
        // Given
        List<CashMovement> movements = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            CashMovement movement = new CashMovement();
            movement.setId((long) i);
            movement.setAmount(new BigDecimal(i * 100));
            movement.setType(i % 2 == 0 ? CashMovementType.OUTCOME : CashMovementType.INCOME);
            movement.setLiquidityAccount(liquidityAccount);
            movements.add(movement);
        }

        when(cashMovementRepository.findByPortfolioId(1L)).thenReturn(movements);

        // When
        List<CashMovement> result = cashMovementService.getCashMovementsByUserId(user);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(cashMovementRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    public void testSaveCashMovement_LargeAmount_Success() {
        // Given
        CashMovement largeMovement = new CashMovement();
        largeMovement.setDate(Instant.now());
        largeMovement.setAmount(new BigDecimal("999999999.99"));
        largeMovement.setType(CashMovementType.INCOME);
        largeMovement.setLiquidityAccount(liquidityAccount);

        CashMovement saved = new CashMovement();
        saved.setId(10L);
        saved.setDate(largeMovement.getDate());
        saved.setAmount(largeMovement.getAmount());
        saved.setType(largeMovement.getType());
        saved.setLiquidityAccount(largeMovement.getLiquidityAccount());

        when(cashMovementRepository.save(largeMovement)).thenReturn(saved);

        // When
        CashMovement result = cashMovementService.saveCashMovement(largeMovement);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("999999999.99"), result.getAmount());
        verify(cashMovementRepository, times(1)).save(largeMovement);
    }

    @Test
    public void testSaveCashMovement_WithNoteAndCategory_Success() {
        // Given - All optional fields present
        cashMovement.setNote("Detailed note");
        when(cashMovementRepository.save(cashMovement)).thenReturn(cashMovement);

        // When
        CashMovement result = cashMovementService.saveCashMovement(cashMovement);

        // Then
        assertNotNull(result);
        assertEquals("Detailed note", result.getNote());
        assertNotNull(result.getCategory());
        verify(cashMovementRepository, times(1)).save(cashMovement);
    }
}

