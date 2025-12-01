package com.giuseppesica.maney.illiquidasset.controller;

import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.security.AuthenticationHelper;
import com.giuseppesica.maney.security.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing illiquid assets.
 * Handles HTTP requests for creating, reading, updating, and deleting individual illiquid assets.
 * All endpoints require user authentication and verify asset ownership.
 * Base path follows hierarchical structure: /user/portfolio/illiquid-assets
 */
@RestController
@RequestMapping("/user/portfolio/illiquid-assets")
public class IlliquidAssetController {

    private final IlliquidAssetService illiquidAssetService;
    private final AuthenticationHelper authenticationHelper;
    private final AuthenticationHelper authHelper;

    /**
     * Constructor for dependency injection.
     *
     * @param illiquidAssetService Service for illiquid asset operations
     * @param authenticationHelper Helper for authentication operations
     */
    @Autowired
    public IlliquidAssetController(IlliquidAssetService illiquidAssetService, AuthenticationHelper authenticationHelper, AuthenticationHelper authHelper) {
        this.illiquidAssetService = illiquidAssetService;
        this.authenticationHelper = authenticationHelper;
        this.authHelper = authHelper;
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
        long portfolioId = authenticationHelper.getAuthenticatedUserPortfolioId(authentication);
        IlliquidAsset illiquidAsset = illiquidAssetService.getIlliquidAssetById(portfolioId, assetId)
                .orElseThrow(() -> new NotFoundException("Illiquid Asset not found with ID: " + assetId));

        IlliquidAssetDto assetDto = new IlliquidAssetDto(illiquidAsset);
        return ResponseEntity.ok(assetDto);
    }

    /**
     * Retrieves all illiquid assets in the authenticated user's portfolio.
     *
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with list of IlliquidAssetDto
     */
    @GetMapping
    public ResponseEntity<List<IlliquidAssetDto>> getIlliquidAssets(Authentication authentication) {
        Long portfolioId = authHelper.getAuthenticatedUserPortfolioId(authentication);
        List<IlliquidAssetDto> illiquidAssets = illiquidAssetService.getIlliquidAssets(portfolioId);
        return ResponseEntity.ok(illiquidAssets);
    }

    /**
     * Creates a new illiquid asset for the authenticated user.
     *
     * @param authentication Spring Security authentication object
     * @param illiquidAssetDto DTO containing asset information
     * @return ResponseEntity with created IlliquidAssetDto and status 201
     */
    @PostMapping("")
    public ResponseEntity<IlliquidAssetDto> createIlliquidAsset(
            Authentication authentication,
            @RequestBody IlliquidAssetDto illiquidAssetDto) {
        Portfolio portfolio = authenticationHelper.getAuthenticatedUserPortfolio(authentication);

        IlliquidAsset illiquidAsset = illiquidAssetService.createIlliquidAsset(illiquidAssetDto, portfolio);
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
        Long portfolioId = authenticationHelper.getAuthenticatedUserPortfolioId(authentication);

        Optional<IlliquidAsset> updatedAsset = illiquidAssetService.updateIlliquidAsset(
                portfolioId,
                assetId,
                illiquidAssetDto
        );

        return updatedAsset
                .map(IlliquidAssetDto::new)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Illiquid Asset not found with ID: " + assetId));
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
        Long portfolioId = authenticationHelper.getAuthenticatedUserPortfolioId(authentication);

        Optional<IlliquidAsset> existingAsset = illiquidAssetService.getIlliquidAssetById(
                portfolioId,
                assetId
        );

        if (existingAsset.isEmpty()) {
            throw new NotFoundException("Illiquid Asset not found with ID: " + assetId);
        }

        illiquidAssetService.deleteIlliquidAsset(existingAsset.get());
        return ResponseEntity.status(204).build();
    }
}
