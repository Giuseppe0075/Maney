package com.giuseppesica.maney.account.operation.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.transfer.controller.TransferController;
import com.giuseppesica.maney.account.operation.transfer.model.Transfer;
import com.giuseppesica.maney.account.operation.transfer.model.TransferDto;
import com.giuseppesica.maney.account.operation.transfer.service.TransferService;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.config.SecurityConfig;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
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

@WebMvcTest(TransferController.class)
@Import(SecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private LiquidityAccountService liquidityAccountService;

    private User user;
    private Portfolio portfolio;
    private LiquidityAccount fromAccount;
    private LiquidityAccount toAccount;
    private Transfer transfer;
    private TransferDto transferDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(42L);
        portfolio = new Portfolio();
        portfolio.setId(1L);
        user.setPortfolio(portfolio);

        fromAccount = new LiquidityAccount();
        fromAccount.setId(10L);
        fromAccount.setName("Checking");
        fromAccount.setBalance(new BigDecimal("1000"));
        fromAccount.setCurrency(Currency.EUR);
        fromAccount.setPortfolio(portfolio);

        toAccount = new LiquidityAccount();
        toAccount.setId(11L);
        toAccount.setName("Savings");
        toAccount.setBalance(new BigDecimal("500"));
        toAccount.setCurrency(Currency.EUR);
        toAccount.setPortfolio(portfolio);

        transfer = new Transfer();
        transfer.setId(5L);
        transfer.setAmount(new BigDecimal("250"));
        transfer.setDate(Instant.parse("2024-04-10T12:00:00Z"));
        transfer.setNote("Shift funds");
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);

        transferDto = new TransferDto();
        transferDto.setId(5L);
        transferDto.setAmount(new BigDecimal("250"));
        transferDto.setDate(Instant.parse("2024-04-10T12:00:00Z"));
        transferDto.setNote("Shift funds");
        transferDto.setFromAccountName("Checking");
        transferDto.setToAccountName("Savings");
    }

    @Test
    @WithMockUser
    void testGetAllTransfers_Success() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(transferService.getTransfersByUserId(user)).thenReturn(List.of(transfer));

        mockMvc.perform(get("/user/portfolio/liquidity-accounts/transfers").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromAccountName").value("Checking"))
                .andExpect(jsonPath("$[0].toAccountName").value("Savings"))
                .andExpect(jsonPath("$[0].amount").value(250));

        verify(transferService).getTransfersByUserId(user);
    }

    @Test
    @WithMockUser
    void testGetTransferById_Found() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(transferService.getTransferByIdAndUserId(5L, user)).thenReturn(Optional.of(transfer));

        mockMvc.perform(get("/user/portfolio/liquidity-accounts/transfers/5").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromAccountName").value("Checking"))
                .andExpect(jsonPath("$.amount").value(250));

        verify(transferService).getTransferByIdAndUserId(5L, user);
    }

    @Test
    @WithMockUser
    void testGetTransferById_NotFound() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(transferService.getTransferByIdAndUserId(5L, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/portfolio/liquidity-accounts/transfers/5").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not Found Transfer with id: 5"));

        verify(transferService).getTransferByIdAndUserId(5L, user);
    }

    @Test
    void testGetTransfers_Unauthenticated() throws Exception {
        mockMvc.perform(get("/user/portfolio/liquidity-accounts/transfers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        verify(userService, never()).UserFromAuthentication(any());
    }

    @Test
    @WithMockUser
    void testCreateTransfer_Success() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(liquidityAccountService.getLiquidityAccountByPortfolioIdAndName(anyLong(), eq("Checking")))
                .thenReturn(Optional.of(fromAccount));
        when(liquidityAccountService.getLiquidityAccountByPortfolioIdAndName(anyLong(), eq("Savings")))
                .thenReturn(Optional.of(toAccount));
        when(transferService.saveTransfer(any(Transfer.class))).thenReturn(transfer);

        mockMvc.perform(post("/user/portfolio/liquidity-accounts/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(250))
                .andExpect(jsonPath("$.fromAccountName").value("Checking"));

        verify(liquidityAccountService, times(2)).saveLiquidityAccount(any(LiquidityAccount.class));
        verify(transferService).saveTransfer(any(Transfer.class));
    }

    @Test
    @WithMockUser
    void testCreateTransfer_FromAccountNotFound() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(liquidityAccountService.getLiquidityAccountByPortfolioIdAndName(anyLong(), eq("Checking")))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/user/portfolio/liquidity-accounts/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Liquidity Account not found with name: Checking"));

        verify(liquidityAccountService, never()).saveLiquidityAccount(any());
        verify(transferService, never()).saveTransfer(any());
    }

    @Test
    @WithMockUser
    void testUpdateTransfer_Success() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(transferService.getTransferByIdAndUserId(5L, user)).thenReturn(Optional.of(transfer));
        when(liquidityAccountService.getLiquidityAccountByPortfolioIdAndName(anyLong(), eq("Checking")))
                .thenReturn(Optional.of(fromAccount));
        when(liquidityAccountService.getLiquidityAccountByPortfolioIdAndName(anyLong(), eq("Savings")))
                .thenReturn(Optional.of(toAccount));
        when(transferService.saveTransfer(any(Transfer.class))).thenReturn(transfer);

        mockMvc.perform(put("/user/portfolio/liquidity-accounts/transfers/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(250));

        verify(liquidityAccountService, times(4)).saveLiquidityAccount(any(LiquidityAccount.class));
        verify(transferService).saveTransfer(any(Transfer.class));
    }

    @Test
    @WithMockUser
    void testUpdateTransfer_NotFound() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(transferService.getTransferByIdAndUserId(5L, user)).thenReturn(Optional.empty());

        mockMvc.perform(put("/user/portfolio/liquidity-accounts/transfers/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not Found Transfer with id: 5"));

        verify(transferService).getTransferByIdAndUserId(5L, user);
        verify(liquidityAccountService, never()).saveLiquidityAccount(any());
    }

    @Test
    @WithMockUser
    void testDeleteTransfer_Success() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(transferService.getTransferByIdAndUserId(5L, user)).thenReturn(Optional.of(transfer));
        doNothing().when(transferService).deleteTransferById(5L);

        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/transfers/5").with(csrf()))
                .andExpect(status().isNoContent());

        verify(liquidityAccountService, times(2)).saveLiquidityAccount(any(LiquidityAccount.class));
        verify(transferService).deleteTransferById(5L);
    }

    @Test
    @WithMockUser
    void testDeleteTransfer_NotFound() throws Exception {
        when(userService.UserFromAuthentication(any())).thenReturn(user);
        when(transferService.getTransferByIdAndUserId(5L, user)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/user/portfolio/liquidity-accounts/transfers/5").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not Found Transfer with id: 5"));

        verify(transferService).getTransferByIdAndUserId(5L, user);
        verify(transferService, never()).deleteTransferById(anyLong());
        verify(liquidityAccountService, never()).saveLiquidityAccount(any());
    }

    @Test
    void testCreateTransfer_Unauthenticated() throws Exception {
        mockMvc.perform(post("/user/portfolio/liquidity-accounts/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isForbidden());

        verify(userService, never()).UserFromAuthentication(any());
    }
}
