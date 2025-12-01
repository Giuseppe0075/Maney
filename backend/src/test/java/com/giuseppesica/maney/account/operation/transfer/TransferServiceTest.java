package com.giuseppesica.maney.account.operation.transfer;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.transfer.model.Transfer;
import com.giuseppesica.maney.account.operation.transfer.model.TransferRepository;
import com.giuseppesica.maney.account.operation.transfer.service.TransferService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.utils.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private TransferService transferService;

    private User user;
    private Portfolio portfolio;
    private LiquidityAccount fromAccount;
    private LiquidityAccount toAccount;
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        portfolio.setId(1L);

        user = new User();
        user.setId(42L);
        user.setPortfolio(portfolio);

        fromAccount = new LiquidityAccount();
        fromAccount.setId(10L);
        fromAccount.setName("Primary");
        fromAccount.setCurrency(Currency.EUR);
        fromAccount.setBalance(new BigDecimal("1000"));
        fromAccount.setPortfolio(portfolio);
        fromAccount.setOpenedAt(Instant.parse("2024-01-01T10:00:00Z"));

        toAccount = new LiquidityAccount();
        toAccount.setId(11L);
        toAccount.setName("Savings");
        toAccount.setCurrency(Currency.EUR);
        toAccount.setBalance(new BigDecimal("500"));
        toAccount.setPortfolio(portfolio);
        toAccount.setOpenedAt(Instant.parse("2024-01-01T10:00:00Z"));

        transfer = new Transfer();
        transfer.setId(5L);
        transfer.setAmount(new BigDecimal("150"));
        transfer.setDate(Instant.parse("2024-03-15T12:00:00Z"));
        transfer.setNote("Monthly move");
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
    }

    @Test
    void testGetTransfersByUserId_ReturnsTransfers() {
        when(transferRepository.findByPortfolioId(1L)).thenReturn(List.of(transfer));

        List<Transfer> result = transferService.getTransfersByUserId(user);

        assertEquals(1, result.size());
        assertEquals(transfer, result.get(0));
        verify(transferRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    void testGetTransfersByUserId_EmptyList() {
        when(transferRepository.findByPortfolioId(1L)).thenReturn(List.of());

        List<Transfer> result = transferService.getTransfersByUserId(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transferRepository, times(1)).findByPortfolioId(1L);
    }

    @Test
    void testGetTransferByIdAndUserId_Found() {
        when(transferRepository.findByIdAndPortfolioId(5L, 1L)).thenReturn(Optional.of(transfer));

        Optional<Transfer> result = transferService.getTransferByIdAndUserId(5L, user);

        assertTrue(result.isPresent());
        assertEquals(transfer, result.get());
        verify(transferRepository, times(1)).findByIdAndPortfolioId(5L, 1L);
    }

    @Test
    void testGetTransferByIdAndUserId_NotFound() {
        when(transferRepository.findByIdAndPortfolioId(99L, 1L)).thenReturn(Optional.empty());

        Optional<Transfer> result = transferService.getTransferByIdAndUserId(99L, user);

        assertTrue(result.isEmpty());
        verify(transferRepository, times(1)).findByIdAndPortfolioId(99L, 1L);
    }

    @Test
    void testSaveTransfer_PersistsTransfer() {
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        Transfer result = transferService.saveTransfer(transfer);

        assertNotNull(result);
        assertEquals(new BigDecimal("150"), result.getAmount());
        verify(transferRepository, times(1)).save(transfer);
    }

    @Test
    void testDeleteTransferById_DelegatesToRepository() {
        doNothing().when(transferRepository).deleteById(5L);

        transferService.deleteTransferById(5L);

        verify(transferRepository, times(1)).deleteById(5L);
    }
}

