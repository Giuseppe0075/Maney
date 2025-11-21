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

/**
 * REST controller for managing illiquid assets.
 * Handles HTTP requests for creating, reading, updating, and deleting illiquid assets.
 * All endpoints require user authentication and verify asset ownership.
 */
@Controller
@RequestMapping("/user/illiquid-asset")
public class IlliquidAssetController {

    private final IlliquidAssetService illiquidAssetService;
    private final UserService userService;
    private final PortfolioService portfolioService;

    /**
     * Constructor for dependency injection.
     *
     * @param illiquidAssetService Service for illiquid asset operations
     * @param userService Service for user operations
     * @param portfolioService Service for portfolio operations
     */
    @Autowired
    public IlliquidAssetController(IlliquidAssetService illiquidAssetService, UserService userService, PortfolioService portfolioService) {
        this.illiquidAssetService = illiquidAssetService;
        this.userService = userService;
        this.portfolioService = portfolioService;
    }

    /**
     * Retrieves a specific illiquid asset by ID.
     * Verifies that the asset belongs to the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @param assetId ID of the asset to retrieve
     * @return ResponseEntity with IlliquidAssetDto if found, 404 otherwise
     */
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

    /**
     * Creates a new illiquid asset for the authenticated user.
     *
     * @param authentication Spring Security authentication object
     * @param illiquidAssetDto DTO containing asset information
     * @return ResponseEntity with created IlliquidAssetDto and status 201, or 404 if user/portfolio not found
     */
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

    /**
     * Updates an existing illiquid asset.
     * Verifies that the asset belongs to the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @param assetId ID of the asset to update
     * @param illiquidAssetDto DTO containing updated asset information
     * @return ResponseEntity with updated IlliquidAssetDto if successful, 404 otherwise
     */
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

    /**
     * Deletes an illiquid asset.
     * Verifies that the asset belongs to the authenticated user's portfolio before deletion.
     *
     * @param authentication Spring Security authentication object
     * @param assetId ID of the asset to delete
     * @return ResponseEntity with status 204 if successful, 404 if asset not found
     */
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
