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

/**
 * REST controller for managing liquidity accounts.
 * Handles HTTP requests related to creating, reading, updating, and deleting individual liquidity accounts.
 * All endpoints require user authentication and validate portfolio ownership.
 * Base path follows hierarchical structure: /user/portfolio/liquidity-accounts
 */
@RestController
@RequestMapping("/user/portfolio/liquidity-accounts")
public class LiquidityAccountController {
    private final LiquidityAccountService liquidityAccountService;
    private final AuthenticationHelper authHelper;

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
     * Validates that the portfolio ID in the request belongs to the authenticated user.
     *
     * @param authentication Spring Security authentication object
     * @param liquidityAccountDto DTO containing liquidity account data
     * @return ResponseEntity with created liquidity account data
     */
    @PostMapping
    public ResponseEntity<LiquidityAccountDto> createLiquidityAccount(
            Authentication authentication,
            @Valid @RequestBody LiquidityAccountDto liquidityAccountDto
    ) {
        // Validate that the portfolio belongs to the authenticated user
        authHelper.validatePortfolioAccess(authentication, liquidityAccountDto.getPortfolioId());

        // Create and save the liquidity account
        LiquidityAccount liquidityAccount = liquidityAccountService.saveLiquidityAccount(liquidityAccountDto);
        LiquidityAccountDto responseDto = new LiquidityAccountDto(liquidityAccount);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Retrieves a specific liquidity account by ID.
     * Validates that the account belongs to the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @param id liquidity account ID
     * @return ResponseEntity with liquidity account data
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
     * Updates an existing liquidity account.
     * Validates that the account belongs to the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @param id liquidity account ID
     * @param liquidityAccountDto DTO containing updated account data
     * @return ResponseEntity with updated liquidity account data
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
     * Validates that the account belongs to the authenticated user's portfolio before deletion.
     *
     * @param authentication Spring Security authentication object
     * @param id liquidity account ID
     * @return ResponseEntity with status 204 if successful
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
