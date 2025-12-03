package com.giuseppesica.maney.portfolio.controller;

import com.giuseppesica.maney.account.liquidityaccount.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.liquidityaccount.service.LiquidityAccountService;
import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.dto.PortfolioDto;
import com.giuseppesica.maney.portfolio.model.Portfolio;
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
    private final AuthenticationHelper authenticationHelper;

    /**
     * Constructor for dependency injection.
     *
     * @param illiquidAssetService Service for illiquid asset operations
     * @param liquidityAccountService Service for liquidity account operations
     * @param authenticationHelper Helper for authentication operations
     */
    @Autowired
    public PortfolioController(
            IlliquidAssetService illiquidAssetService,
            LiquidityAccountService liquidityAccountService,
            AuthenticationHelper authenticationHelper
    ) {
        this.illiquidAssetService = illiquidAssetService;
        this.liquidityAccountService = liquidityAccountService;
        this.authenticationHelper = authenticationHelper;
    }

    /**
     * Retrieves the portfolio of the authenticated user.
     * Includes all illiquid assets and liquidity accounts in the portfolio.
     *
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with PortfolioDto containing portfolio data and assets
     */
    @GetMapping
    public ResponseEntity<PortfolioDto> getPortfolio(Authentication authentication) {
        // Get authenticated user's portfolio using helper
        Portfolio portfolio = authenticationHelper.getAuthenticatedUserPortfolio(authentication);
        Long portfolioId = portfolio.getId();

        // Retrieve all assets
        List<IlliquidAssetDto> illiquidAssetDtos = illiquidAssetService.getIlliquidAssets(portfolioId);
        List<LiquidityAccountDto> liquidityAccountDtos = liquidityAccountService.getLiquidityAccounts(portfolioId);

        // Create and return PortfolioDto
        PortfolioDto portfolioDto = new PortfolioDto(portfolio);
        portfolioDto.setIlliquidAssets(illiquidAssetDtos);
        portfolioDto.setLiquidityAccounts(liquidityAccountDtos);

        return ResponseEntity.ok(portfolioDto);
    }
}
