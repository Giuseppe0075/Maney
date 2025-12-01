package com.giuseppesica.maney.account.operation.transfer.controller;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.transfer.model.Transfer;
import com.giuseppesica.maney.account.operation.transfer.model.TransferDto;
import com.giuseppesica.maney.account.operation.transfer.service.TransferService;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/portfolio/liquidity-accounts/transfers")
public class TransferController {

    private final UserService userService;
    private final TransferService transferService;
    private final LiquidityAccountService liquidityAccountService;

    @Autowired
    public TransferController(UserService userService, TransferService transferService, LiquidityAccountService liquidityAccountService) {
        this.userService = userService;
        this.transferService = transferService;
        this.liquidityAccountService = liquidityAccountService;
    }

    private List<LiquidityAccount> updateAccounts(Long portfolioId, String fromAccountName, String toAccountName, java.math.BigDecimal amount) {
        LiquidityAccount from = liquidityAccountService
                .getLiquidityAccountByPortfolioIdAndName(portfolioId, fromAccountName)
                .orElseThrow(() -> new NotFoundException("Liquidity Account not found with name: " + fromAccountName));
        LiquidityAccount to = liquidityAccountService
                .getLiquidityAccountByPortfolioIdAndName(portfolioId, toAccountName)
                .orElseThrow(() -> new NotFoundException("Liquidity Account not found with name: " + toAccountName));

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        liquidityAccountService.saveLiquidityAccount(from);
        liquidityAccountService.saveLiquidityAccount(to);

        return java.util.Arrays.asList(from, to);
    }

    @GetMapping
    public ResponseEntity<List<TransferDto>> getAllTransfers(
            Authentication authentication
    ){
        User user = userService.UserFromAuthentication(authentication);
        List<Transfer> transfers = transferService.getTransfersByUserId(user);
        List<TransferDto> transferDtos =
                transfers.stream()
                        .map(TransferDto::new)
                        .toList();
        return ResponseEntity.ok(transferDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferDto> getTransferById(
            Authentication authentication,
            @PathVariable Long id
    ){
        User user = userService.UserFromAuthentication(authentication);
        Transfer transfer = transferService.getTransferByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Not Found Transfer with id: " + id));
        TransferDto transferDto = new TransferDto(transfer);
        return ResponseEntity.ok(transferDto);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TransferDto> createTransferById(
            Authentication authentication,
            @RequestBody TransferDto transferDto
    ){
        User user = userService.UserFromAuthentication(authentication);
        List<LiquidityAccount> accounts = updateAccounts(
                user.getPortfolio().getId(),
                transferDto.getFromAccountName(),
                transferDto.getToAccountName(),
                transferDto.getAmount()
        );
        LiquidityAccount fromAccount = accounts.get(0);
        LiquidityAccount toAccount = accounts.get(1);

        Transfer transfer = new Transfer();
        transfer.setAmount(transferDto.getAmount());
        transfer.setDate(transferDto.getDate());
        transfer.setNote(transferDto.getNote());
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer = transferService.saveTransfer(transfer);
        TransferDto createdTransferDto = new TransferDto(transfer);

        return ResponseEntity.ok(createdTransferDto);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<TransferDto> updateTransferById(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody TransferDto transferDto
    ){
        User user = userService.UserFromAuthentication(authentication);
        Transfer existingTransfer = transferService.getTransferByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Not Found Transfer with id: " + id));

        LiquidityAccount previousFrom = existingTransfer.getFromAccount();
        LiquidityAccount previousTo = existingTransfer.getToAccount();

        previousFrom.setBalance(previousFrom.getBalance().add(existingTransfer.getAmount()));
        previousTo.setBalance(previousTo.getBalance().subtract(existingTransfer.getAmount()));
        liquidityAccountService.saveLiquidityAccount(previousFrom);
        liquidityAccountService.saveLiquidityAccount(previousTo);

        List<LiquidityAccount> updatedAccounts = updateAccounts(
                user.getPortfolio().getId(),
                transferDto.getFromAccountName(),
                transferDto.getToAccountName(),
                transferDto.getAmount()
        );
        LiquidityAccount newFromAccount = updatedAccounts.get(0);
        LiquidityAccount newToAccount = updatedAccounts.get(1);

        existingTransfer.setAmount(transferDto.getAmount());
        existingTransfer.setDate(transferDto.getDate());
        existingTransfer.setNote(transferDto.getNote());
        existingTransfer.setFromAccount(newFromAccount);
        existingTransfer.setToAccount(newToAccount);

        Transfer updatedTransfer = transferService.saveTransfer(existingTransfer);
        return ResponseEntity.ok(new TransferDto(updatedTransfer));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteTransferById(
            Authentication authentication,
            @PathVariable Long id
    ){
        User user = userService.UserFromAuthentication(authentication);
        Transfer existingTransfer = transferService.getTransferByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Not Found Transfer with id: " + id));

        LiquidityAccount fromAccount = existingTransfer.getFromAccount();
        LiquidityAccount toAccount = existingTransfer.getToAccount();

        fromAccount.setBalance(fromAccount.getBalance().add(existingTransfer.getAmount()));
        toAccount.setBalance(toAccount.getBalance().subtract(existingTransfer.getAmount()));
        liquidityAccountService.saveLiquidityAccount(fromAccount);
        liquidityAccountService.saveLiquidityAccount(toAccount);

        transferService.deleteTransferById(id);
        return ResponseEntity.noContent().build();
    }
}
