package com.giuseppesica.maney.portfolio.controller;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.service.PortfolioService;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing user portfolios.
 * Handles HTTP requests related to portfolio data and its assets.
 * All endpoints require user authentication.
 */
@Controller
@RequestMapping("/user/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final IlliquidAssetService illiquidAssetService;
    private final UserService userService;
    private final LiquidityAccountService liquidityAccountService;

    /**
     * Constructor for dependency injection.
     *
     * @param portfolioService Service for portfolio operations
     * @param illiquidAssetService Service for illiquid asset operations
     * @param userService Service for user operations
     */
    @Autowired
    public PortfolioController(PortfolioService portfolioService, IlliquidAssetService illiquidAssetService, UserService userService, LiquidityAccountService liquidityAccountService) {
        this.portfolioService = portfolioService;
        this.illiquidAssetService = illiquidAssetService;
        this.userService = userService;
        this.liquidityAccountService = liquidityAccountService;
    }

    protected Portfolio getAuthenticatedUserPortfolio(Authentication authentication) throws Exception {
        User user = userService.UserFromAuthentication(authentication);
        Optional<Portfolio> portfolio = portfolioService.findByUserId(user.getId());
        if (portfolio.isEmpty()) {
            throw new Exception("Portfolio not found for user ID: " + user.getId());
        }
        return portfolio.get();
    }

    /**
     * Retrieves all illiquid assets in the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with list of IlliquidAssetDto, or 404 if user/portfolio not found
     */
    @GetMapping("/illiquid-assets")
    public ResponseEntity<List<IlliquidAssetDto>> getIlliquidAssets(Authentication authentication) {
        long portfolioId;
        try {
            portfolioId = getAuthenticatedUserPortfolio(authentication).getId();
        }catch (Exception e){
            return ResponseEntity.status(404).build();
        }
        List<IlliquidAssetDto> illiquidAssets =
                illiquidAssetService.getIlliquidAssets(portfolioId);

        return ResponseEntity.ok(illiquidAssets);
    }

    @GetMapping("/liquidity-accounts")
    public ResponseEntity<List<LiquidityAccountDto>> getLiquidityAccounts(Authentication authentication) {
        long portfolioId;
        try {
            portfolioId = getAuthenticatedUserPortfolio(authentication).getId();
        }catch (Exception e){
            return ResponseEntity.status(404).build();
        }
        List<LiquidityAccountDto> liquidityAccounts =
                liquidityAccountService.getLiquidityAccounts(portfolioId);

        return ResponseEntity.ok(liquidityAccounts);
    }
}
