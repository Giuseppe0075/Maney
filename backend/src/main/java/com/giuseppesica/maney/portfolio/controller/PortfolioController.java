package com.giuseppesica.maney.portfolio.controller;

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

@Controller
@RequestMapping("/user/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final IlliquidAssetService illiquidAssetService;
    private final UserService userService;

    @Autowired
    public PortfolioController(PortfolioService portfolioService, IlliquidAssetService illiquidAssetService, UserService userService) {
        this.portfolioService = portfolioService;
        this.illiquidAssetService = illiquidAssetService;
        this.userService = userService;
    }

    @GetMapping("/illiquid-assets")
    public ResponseEntity<List<IlliquidAssetDto>> getIlliquidAssets(Authentication authentication) {
        User user;
        try{
            user = userService.UserFromAuthentication(authentication);
        }
        catch (Exception e){
            return ResponseEntity.status(404).build();
        }

        Optional<Portfolio> portfolio= portfolioService.findByUserId(user.getId());
        if (portfolio.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        Long portfolioId = portfolio.get().getId();
        List<IlliquidAssetDto> illiquidAssets =
                illiquidAssetService.getIlliquidAssets(portfolioId);

        return ResponseEntity.ok(illiquidAssets);
    }
}
