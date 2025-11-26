package com.giuseppesica.maney.portfolio.controller;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.security.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing user portfolios.
 * Handles HTTP requests related to portfolio data and its assets.
 * All endpoints require user authentication.
 */
@RestController
@RequestMapping("/user/portfolio")
public class PortfolioController {
    private final IlliquidAssetService illiquidAssetService;
    private final LiquidityAccountService liquidityAccountService;
    private final AuthenticationHelper authHelper;

    /**
     * Constructor for dependency injection.
     *
     * @param illiquidAssetService Service for illiquid asset operations
     * @param liquidityAccountService Service for liquidity account operations
     * @param authHelper Helper for authentication operations
     */
    @Autowired
    public PortfolioController(
            IlliquidAssetService illiquidAssetService,
            LiquidityAccountService liquidityAccountService,
            AuthenticationHelper authHelper
    ) {
        this.illiquidAssetService = illiquidAssetService;
        this.liquidityAccountService = liquidityAccountService;
        this.authHelper = authHelper;
    }

    /**
     * Retrieves all illiquid assets in the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with list of IlliquidAssetDto
     */
    @GetMapping("/illiquid-assets")
    public ResponseEntity<List<IlliquidAssetDto>> getIlliquidAssets(Authentication authentication) {
        Long portfolioId = authHelper.getAuthenticatedUserPortfolioId(authentication);
        List<IlliquidAssetDto> illiquidAssets = illiquidAssetService.getIlliquidAssets(portfolioId);
        return ResponseEntity.ok(illiquidAssets);
    }

    /**
     * Retrieves all liquidity accounts in the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with list of LiquidityAccountDto
     */
    @GetMapping("/liquidity-accounts")
    public ResponseEntity<List<LiquidityAccountDto>> getLiquidityAccounts(Authentication authentication) {
        Long portfolioId = authHelper.getAuthenticatedUserPortfolioId(authentication);
        List<LiquidityAccountDto> liquidityAccounts = liquidityAccountService.getLiquidityAccounts(portfolioId);
        return ResponseEntity.ok(liquidityAccounts);
    }
}
