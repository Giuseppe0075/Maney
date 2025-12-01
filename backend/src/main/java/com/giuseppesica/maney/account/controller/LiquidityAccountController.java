package com.giuseppesica.maney.account.controller;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.security.AuthenticationHelper;
import com.giuseppesica.maney.security.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing liquidity accounts.
 *
 * <p>Handles HTTP requests related to creating, reading, updating, and deleting individual
 * liquidity accounts. Each account represents a cash or checking account that can hold funds
 * and participate in transactions.</p>
 *
 * <p>All endpoints require user authentication and validate portfolio ownership to ensure
 * users can only manage accounts within their own portfolio.</p>
 *
 * <p><strong>Base Path:</strong> {@code /user/portfolio/liquidity-accounts}</p>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All endpoints require Spring Security authentication</li>
 *   <li>{@link AuthenticationHelper} validates portfolio/resource ownership</li>
 *   <li>Operations fail with 403/404 if user lacks access to the requested resource</li>
 * </ul>
 *
 * @see LiquidityAccount
 * @see LiquidityAccountDto
 * @see LiquidityAccountService
 * @see AuthenticationHelper
 */
@RestController
@RequestMapping("/user/portfolio/liquidity-accounts")
public class LiquidityAccountController {
    private final LiquidityAccountService liquidityAccountService;
    private final AuthenticationHelper authHelper;

    /**
     * Constructs the controller with required dependencies.
     *
     * @param liquidityAccountService service layer for account operations
     * @param authHelper helper for authentication and authorization validation
     */
    @Autowired
    public LiquidityAccountController(
            LiquidityAccountService liquidityAccountService,
            AuthenticationHelper authHelper
    ) {
        this.liquidityAccountService = liquidityAccountService;
        this.authHelper = authHelper;
    }

    /**
     * Creates a new liquidity account for the authenticated user.
     *
     * <p>Validates that the portfolio ID in the request belongs to the authenticated user
     * before persisting the account. The account will be linked to the specified portfolio.</p>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>
     * POST /user/portfolio/liquidity-accounts
     * {
     *   "name": "Checking Account",
     *   "institution": "Bank of Example",
     *   "balance": 1000.00,
     *   "currency": "EUR",
     *   "portfolioId": 5
     * }
     * </pre>
     *
     * @param authentication Spring Security authentication object
     * @param liquidityAccountDto DTO containing liquidity account data
     * @return ResponseEntity with HTTP 200 and created account data
     * @throws IllegalArgumentException if portfolio doesn't belong to user
     */
    @PostMapping
    public ResponseEntity<LiquidityAccountDto> createLiquidityAccount(
            Authentication authentication,
            @Valid @RequestBody LiquidityAccountDto liquidityAccountDto
    ) {
        // Validate that the portfolio belongs to the authenticated user
        authHelper.validatePortfolioAccess(authentication, liquidityAccountDto.getPortfolioId());

        // Create and save the liquidity account
        LiquidityAccount liquidityAccount = new LiquidityAccount(liquidityAccountDto);
        liquidityAccount = liquidityAccountService.saveLiquidityAccount(liquidityAccount);
        LiquidityAccountDto responseDto = new LiquidityAccountDto(liquidityAccount);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Retrieves a specific liquidity account by ID.
     *
     * <p>Fetches the account and validates that it belongs to the authenticated user's
     * portfolio before returning the data. This ensures users can only view their own accounts.</p>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>
     * GET /user/portfolio/liquidity-accounts/42
     * </pre>
     *
     * @param authentication Spring Security authentication object
     * @param id liquidity account ID
     * @return ResponseEntity with HTTP 200 and account data
     * @throws NotFoundException if account doesn't exist
     * @throws IllegalArgumentException if account doesn't belong to user's portfolio
     */
    @GetMapping("/{id}")
    public ResponseEntity<LiquidityAccountDto> getLiquidityAccount(
            Authentication authentication,
            @PathVariable Long id
    ) {
        // Get the liquidity account
        LiquidityAccount liquidityAccount = liquidityAccountService.getLiquidityAccountById(id)
                .orElseThrow(() -> new NotFoundException("Liquidity account not found with ID: " + id));

        // Validate that the account belongs to the user's portfolio
        Long accountPortfolioId = liquidityAccount.getPortfolio().getId();
        authHelper.validateResourceAccess(authentication, accountPortfolioId, "LiquidityAccount");

        LiquidityAccountDto responseDto = new LiquidityAccountDto(liquidityAccount);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Retrieves all liquidity accounts in the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with list of LiquidityAccountDto
     */
    @GetMapping
    public ResponseEntity<List<LiquidityAccountDto>> getLiquidityAccounts(Authentication authentication) {
        Long portfolioId = authHelper.getAuthenticatedUserPortfolioId(authentication);
        List<LiquidityAccountDto> liquidityAccounts = liquidityAccountService.getLiquidityAccounts(portfolioId);
        return ResponseEntity.ok(liquidityAccounts);
    }

    /**
     * Updates an existing liquidity account.
     *
     * <p>Validates ownership before applying updates. All fields in the DTO will replace
     * the corresponding values in the existing account.</p>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>
     * PUT /user/portfolio/liquidity-accounts/42
     * {
     *   "name": "Updated Account Name",
     *   "institution": "New Bank",
     *   "balance": 2500.00,
     *   "currency": "USD"
     * }
     * </pre>
     *
     * @param authentication Spring Security authentication object
     * @param id liquidity account ID to update
     * @param liquidityAccountDto DTO containing updated account data
     * @return ResponseEntity with HTTP 200 and updated account data
     * @throws NotFoundException if account doesn't exist
     * @throws IllegalArgumentException if account doesn't belong to user's portfolio
     */
    @PutMapping("/{id}")
    public ResponseEntity<LiquidityAccountDto> updateLiquidityAccount(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody LiquidityAccountDto liquidityAccountDto
    ) {
        // First verify the account exists and belongs to the user
        LiquidityAccount existingAccount = liquidityAccountService.getLiquidityAccountById(id)
                .orElseThrow(() -> new NotFoundException("Liquidity account not found with ID: " + id));

        Long accountPortfolioId = existingAccount.getPortfolio().getId();
        authHelper.validateResourceAccess(authentication, accountPortfolioId, "LiquidityAccount");

        // Update the account
        LiquidityAccount updatedAccount = liquidityAccountService.updateLiquidityAccount(id, liquidityAccountDto);
        LiquidityAccountDto responseDto = new LiquidityAccountDto(updatedAccount);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Deletes a liquidity account.
     *
     * <p>Validates ownership before deletion. Once deleted, the account and its history
     * are permanently removed from the database.</p>
     *
     * <p><strong>Warning:</strong> Deletion is irreversible. Consider archiving or marking
     * as closed instead if transaction history needs to be preserved.</p>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>
     * DELETE /user/portfolio/liquidity-accounts/42
     * </pre>
     *
     * @param authentication Spring Security authentication object
     * @param id liquidity account ID to delete
     * @return ResponseEntity with HTTP 204 (No Content) on successful deletion
     * @throws NotFoundException if account doesn't exist
     * @throws IllegalArgumentException if account doesn't belong to user's portfolio
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiquidityAccount(
            Authentication authentication,
            @PathVariable Long id
    ) {
        // Verify the account exists and belongs to the user
        LiquidityAccount existingAccount = liquidityAccountService.getLiquidityAccountById(id)
                .orElseThrow(() -> new NotFoundException("Liquidity account not found with ID: " + id));

        Long accountPortfolioId = existingAccount.getPortfolio().getId();
        authHelper.validateResourceAccess(authentication, accountPortfolioId, "LiquidityAccount");

        // Delete the account
        liquidityAccountService.deleteLiquidityAccount(id);

        return ResponseEntity.status(204).build();
    }
}
