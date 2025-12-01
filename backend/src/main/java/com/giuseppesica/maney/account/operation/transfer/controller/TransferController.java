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

/**
 * REST controller for managing transfers between liquidity accounts.
 *
 * <p>This controller handles all HTTP operations related to transferring funds between
 * liquidity accounts within a user's portfolio. Each transfer operation updates the
 * balances of both the source and destination accounts atomically.</p>
 *
 * <p>All endpoints follow the hierarchical structure:
 * {@code /user/portfolio/liquidity-accounts/transfers} and require authentication.
 * Each operation validates that the referenced accounts belong to the authenticated user's portfolio.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Atomic balance updates using {@code @Transactional}</li>
 *   <li>Automatic reversal of previous balances on update/delete</li>
 *   <li>Account resolution by name within user's portfolio</li>
 *   <li>Full CRUD operations with proper authorization checks</li>
 * </ul>
 *
 * @see Transfer
 * @see TransferDto
 * @see TransferService
 * @see LiquidityAccountService
 */
@RestController
@RequestMapping("/user/portfolio/liquidity-accounts/transfers")
public class TransferController {

    private final UserService userService;
    private final TransferService transferService;
    private final LiquidityAccountService liquidityAccountService;

    /**
     * Constructs the TransferController with required dependencies.
     *
     * @param userService service for user authentication and retrieval
     * @param transferService service for transfer persistence and queries
     * @param liquidityAccountService service for account lookups and balance updates
     */
    @Autowired
    public TransferController(UserService userService, TransferService transferService, LiquidityAccountService liquidityAccountService) {
        this.userService = userService;
        this.transferService = transferService;
        this.liquidityAccountService = liquidityAccountService;
    }

    /**
     * Updates account balances for a transfer operation.
     *
     * <p>This helper method centralizes the logic for debiting the source account
     * and crediting the destination account. It's used by both create and update
     * operations to avoid code duplication.</p>
     *
     * <p>The method performs the following steps:</p>
     * <ol>
     *   <li>Resolves both accounts by name within the specified portfolio</li>
     *   <li>Subtracts the amount from the source account balance</li>
     *   <li>Adds the amount to the destination account balance</li>
     *   <li>Persists both updated accounts</li>
     * </ol>
     *
     * @param portfolioId the portfolio ID to search within
     * @param fromAccountName name of the source account (debited)
     * @param toAccountName name of the destination account (credited)
     * @param amount the transfer amount (must be positive)
     * @return list containing [fromAccount, toAccount] with updated balances
     * @throws NotFoundException if either account is not found in the portfolio
     */
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

    /**
     * Retrieves all transfers for the authenticated user.
     *
     * <p>Returns a list of all transfers where the source account belongs to the
     * user's portfolio. The list is ordered by the repository's default ordering.</p>
     *
     * @param authentication Spring Security authentication object containing user details
     * @return ResponseEntity with HTTP 200 and list of transfer DTOs (may be empty)
     */
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

    /**
     * Retrieves a specific transfer by its ID.
     *
     * <p>Fetches a single transfer and validates that it belongs to the authenticated
     * user's portfolio. This ensures users can only access their own transfer records.</p>
     *
     * @param authentication Spring Security authentication object containing user details
     * @param id the transfer ID to retrieve
     * @return ResponseEntity with HTTP 200 and the transfer DTO
     * @throws NotFoundException if the transfer doesn't exist or doesn't belong to the user
     */
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

    /**
     * Creates a new transfer between two liquidity accounts.
     *
     * <p>This endpoint performs the following operations atomically:</p>
     * <ol>
     *   <li>Validates both accounts exist in the user's portfolio</li>
     *   <li>Debits the specified amount from the source account</li>
     *   <li>Credits the same amount to the destination account</li>
     *   <li>Persists the transfer record with references to both accounts</li>
     * </ol>
     *
     * <p>The entire operation is wrapped in a transaction to ensure data consistency.
     * If any step fails, all balance changes are rolled back.</p>
     *
     * @param authentication Spring Security authentication object containing user details
     * @param transferDto DTO containing transfer details (accounts, amount, date, note)
     * @return ResponseEntity with HTTP 200 and the created transfer DTO
     * @throws NotFoundException if either account is not found in the user's portfolio
     */
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

    /**
     * Updates an existing transfer with new details.
     *
     * <p>This endpoint handles transfer modifications by performing a two-phase update:</p>
     * <ol>
     *   <li><strong>Reversal Phase:</strong> Restores the original account balances by
     *       reversing the previous transfer (adds amount back to source, subtracts from destination)</li>
     *   <li><strong>Application Phase:</strong> Applies the new transfer details including
     *       potentially different accounts, amount, date, or notes</li>
     * </ol>
     *
     * <p>This approach ensures balance integrity even when the accounts or amount change.
     * The entire operation is transactional - if the update fails, balances remain unchanged.</p>
     *
     * @param authentication Spring Security authentication object containing user details
     * @param id the ID of the transfer to update
     * @param transferDto DTO containing updated transfer details
     * @return ResponseEntity with HTTP 200 and the updated transfer DTO
     * @throws NotFoundException if the transfer or either account is not found
     */
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

    /**
     * Deletes a transfer and reverts its financial impact.
     *
     * <p>Removes the transfer record from the database while automatically reversing
     * its effect on account balances:</p>
     * <ul>
     *   <li>Adds the transfer amount back to the source account</li>
     *   <li>Subtracts the transfer amount from the destination account</li>
     * </ul>
     *
     * <p>The deletion and balance reversal occur within a single transaction to ensure
     * data consistency. If any operation fails, no changes are committed.</p>
     *
     * @param authentication Spring Security authentication object containing user details
     * @param id the ID of the transfer to delete
     * @return ResponseEntity with HTTP 204 (No Content) on successful deletion
     * @throws NotFoundException if the transfer is not found or doesn't belong to the user
     */
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
