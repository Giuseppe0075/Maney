package com.giuseppesica.maney.illiquidasset.controller;

import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.service.PortfolioService;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/user/illiquid-asset")
public class IlliquidAssetController {

    private final IlliquidAssetService illiquidAssetService;
    private final UserService userService;
    private final PortfolioService portfolioService;

    @Autowired
    public IlliquidAssetController(IlliquidAssetService illiquidAssetService, UserService userService, PortfolioService portfolioService) {
        this.illiquidAssetService = illiquidAssetService;
        this.userService = userService;
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<IlliquidAssetDto> getIlliquidAsset(Authentication authentication,
                                                             @PathVariable("id") Long assetId) {
        User user;
        try{
            user = userService.UserFromAuthentication(authentication);
        }
        catch (Exception e){
            return ResponseEntity.status(404).build();
        }

        Optional<Portfolio> portfolio= portfolioService.findByUserId(user.getId());
        return portfolio.map(value -> illiquidAssetService
                .getIlliquidAssetById(value.getId(), assetId)
                .map(IlliquidAssetDto::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build())).orElseGet(() -> ResponseEntity.status(404).build());

    }

    @PostMapping("")
    public ResponseEntity<IlliquidAssetDto> createIlliquidAsset(
            Authentication authentication,
            @RequestBody IlliquidAssetDto illiquidAssetDto) {
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

        IlliquidAsset illiquidAsset = illiquidAssetService.createIlliquidAsset(illiquidAssetDto, portfolio.get());
        IlliquidAssetDto createdIlliquidAssetDto = new IlliquidAssetDto(illiquidAsset);
        return ResponseEntity.status(201).body(createdIlliquidAssetDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IlliquidAssetDto> updateIlliquidAsset(
            Authentication authentication,
            @PathVariable("id") Long assetId,
            @RequestBody IlliquidAssetDto illiquidAssetDto) {
        User user;
        try{
            user = userService.UserFromAuthentication(authentication);
        }
        catch (Exception e){
            return ResponseEntity.status(404).build();
        }

        Optional<Portfolio> portfolio = portfolioService.findByUserId(user.getId());
        if (portfolio.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        Optional<IlliquidAsset> updatedAsset = illiquidAssetService.updateIlliquidAsset(
                portfolio.get().getId(),
                assetId,
                illiquidAssetDto
        );

        return updatedAsset
                .map(IlliquidAssetDto::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIlliquidAsset(
            Authentication authentication,
            @PathVariable("id") Long assetId) {
        User user;
        try {
            user = userService.UserFromAuthentication(authentication);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
        Optional<Portfolio> portfolio = portfolioService.findByUserId(user.getId());
        if (portfolio.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        Optional<IlliquidAsset> existingAsset = illiquidAssetService.getIlliquidAssetById(
                portfolio.get().getId(),
                assetId
        );
        if (existingAsset.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        illiquidAssetService.deleteIlliquidAsset(existingAsset.get());
        return ResponseEntity.status(204).build();
    }
}
