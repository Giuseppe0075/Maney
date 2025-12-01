package com.giuseppesica.maney.portfolio;

import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.model.PortfolioRepository;
import com.giuseppesica.maney.portfolio.service.PortfolioService;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.model.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private User user;
    private Portfolio portfolio;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setUsername("john_doe");

        portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setUser(user);
        user.setPortfolio(portfolio);
    }

    // ==================== GET PORTFOLIO BY USER EMAIL TESTS ====================

    @Test
    public void testGetPortfolioByUserEmail_UserExists_ReturnsPortfolio() {
        // Given
        String email = "john@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<Portfolio> result = portfolioService.getPortfolioByUserEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(portfolio.getId(), result.get().getId());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testGetPortfolioByUserEmail_UserNotFound_ReturnsEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<Portfolio> result = portfolioService.getPortfolioByUserEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testGetPortfolioByUserEmail_UserHasNoPortfolio_ReturnsEmpty() {
        // Given
        String email = "john@example.com";
        User userWithoutPortfolio = new User();
        userWithoutPortfolio.setId(2L);
        userWithoutPortfolio.setEmail(email);
        userWithoutPortfolio.setPortfolio(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithoutPortfolio));

        // When
        Optional<Portfolio> result = portfolioService.getPortfolioByUserEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
    }

    // ==================== UPDATE PORTFOLIO FOR USER TESTS ====================

    @Test
    public void testUpdatePortfolioForUser_Success_ReturnsUpdatedPortfolio() {
        // Given
        String email = "john@example.com";
        Portfolio portfolioData = new Portfolio();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);

        // When
        Portfolio result = portfolioService.updatePortfolioForUser(email, portfolioData);

        // Then
        assertNotNull(result);
        assertEquals(portfolio.getId(), result.getId());
        verify(userRepository, times(1)).findByEmail(email);
        verify(portfolioRepository, times(1)).save(portfolio);
    }

    @Test
    public void testUpdatePortfolioForUser_UserNotFound_ThrowsException() {
        // Given
        String email = "nonexistent@example.com";
        Portfolio portfolioData = new Portfolio();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.updatePortfolioForUser(email, portfolioData));
        assertEquals("User not found: " + email, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    public void testUpdatePortfolioForUser_PortfolioNotFound_ThrowsException() {
        // Given
        String email = "john@example.com";
        Portfolio portfolioData = new Portfolio();
        User userWithoutPortfolio = new User();
        userWithoutPortfolio.setId(2L);
        userWithoutPortfolio.setEmail(email);
        userWithoutPortfolio.setPortfolio(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithoutPortfolio));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.updatePortfolioForUser(email, portfolioData));
        assertEquals("Portfolio not found for user: " + email, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(portfolioRepository, never()).save(any());
    }

    // ==================== DELETE PORTFOLIO FOR USER TESTS ====================

    @Test
    public void testDeletePortfolioForUser_Success_DeletesPortfolio() {
        // Given
        String email = "john@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        portfolioService.deletePortfolioForUser(email);

        // Then
        verify(userRepository, times(1)).findByEmail(email);
        verify(portfolioRepository, times(1)).delete(portfolio);
    }

    @Test
    public void testDeletePortfolioForUser_UserNotFound_ThrowsException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> portfolioService.deletePortfolioForUser(email));
        assertEquals("User not found: " + email, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(portfolioRepository, never()).delete(any());
    }

    @Test
    public void testDeletePortfolioForUser_UserHasNoPortfolio_DoesNothing() {
        // Given
        String email = "john@example.com";
        User userWithoutPortfolio = new User();
        userWithoutPortfolio.setId(2L);
        userWithoutPortfolio.setEmail(email);
        userWithoutPortfolio.setPortfolio(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithoutPortfolio));

        // When
        portfolioService.deletePortfolioForUser(email);

        // Then
        verify(userRepository, times(1)).findByEmail(email);
        verify(portfolioRepository, never()).delete(any());
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    public void testFindById_PortfolioExists_ReturnsPortfolio() {
        // Given
        Long portfolioId = 1L;
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));

        // When
        Optional<Portfolio> result = portfolioService.findById(portfolioId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(portfolioId, result.get().getId());
        verify(portfolioRepository, times(1)).findById(portfolioId);
    }

    @Test
    public void testFindById_PortfolioNotExists_ReturnsEmpty() {
        // Given
        Long portfolioId = 999L;
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        // When
        Optional<Portfolio> result = portfolioService.findById(portfolioId);

        // Then
        assertFalse(result.isPresent());
        verify(portfolioRepository, times(1)).findById(portfolioId);
    }

    // ==================== FIND BY USER ID TESTS ====================

    @Test
    public void testFindByUserId_PortfolioExists_ReturnsPortfolio() {
        // Given
        Long userId = 1L;
        when(portfolioRepository.findByUserId(userId)).thenReturn(Optional.of(portfolio));

        // When
        Optional<Portfolio> result = portfolioService.findByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(portfolio.getId(), result.get().getId());
        verify(portfolioRepository, times(1)).findByUserId(userId);
    }

    @Test
    public void testFindByUserId_PortfolioNotExists_ReturnsEmpty() {
        // Given
        Long userId = 999L;
        when(portfolioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        Optional<Portfolio> result = portfolioService.findByUserId(userId);

        // Then
        assertFalse(result.isPresent());
        verify(portfolioRepository, times(1)).findByUserId(userId);
    }
}

