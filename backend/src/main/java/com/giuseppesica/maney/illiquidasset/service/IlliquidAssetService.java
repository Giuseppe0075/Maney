package com.giuseppesica.maney.illiquidasset.service;

import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAssetRepository;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing illiquid assets.
 * Provides business logic for creating, reading, updating, and deleting illiquid assets.
 */
@Service
public class IlliquidAssetService {

    private final IlliquidAssetRepository illiquidAssetRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param illiquidAssetRepository Repository for illiquid asset data access
     */
    @Autowired
    public IlliquidAssetService(IlliquidAssetRepository illiquidAssetRepository) {
        this.illiquidAssetRepository = illiquidAssetRepository;
    }

    /**
     * Retrieves all illiquid assets belonging to a specific portfolio.
     *
     * @param portfolioId ID of the portfolio
     * @return List of IlliquidAssetDto objects
     */
    public List<IlliquidAssetDto> getIlliquidAssets(Long portfolioId) {
        List<IlliquidAsset> illiquidAssets = illiquidAssetRepository.findByPortfolioId(portfolioId);
        return illiquidAssets.stream()
                .map(IlliquidAssetDto::new)
                .toList();
    }

    /**
     * Retrieves a specific illiquid asset by its ID and portfolio ID.
     * Ensures that the asset belongs to the specified portfolio.
     *
     * @param portfolioId ID of the portfolio
     * @param assetId ID of the asset
     * @return Optional containing the asset if found, empty otherwise
     */
    public Optional<IlliquidAsset> getIlliquidAssetById(Long portfolioId, Long assetId) {
        return illiquidAssetRepository.findByIdAndPortfolioId(assetId, portfolioId);
    }

    /**
     * Creates a new illiquid asset and associates it with a portfolio.
     *
     * @param illiquidAssetDto DTO containing asset information
     * @param portfolio Portfolio to which the asset belongs
     * @return The created IlliquidAsset entity
     */
    public IlliquidAsset createIlliquidAsset(IlliquidAssetDto illiquidAssetDto, Portfolio portfolio) {
        IlliquidAsset illiquidAsset = new IlliquidAsset(illiquidAssetDto);
        illiquidAsset.setPortfolio(portfolio);
        return illiquidAssetRepository.save(illiquidAsset);
    }

    /**
     * Updates an existing illiquid asset.
     * Verifies that the asset belongs to the specified portfolio before updating.
     *
     * @param portfolioId ID of the portfolio
     * @param assetId ID of the asset to update
     * @param illiquidAssetDto DTO containing updated asset information
     * @return Optional containing the updated asset if found, empty otherwise
     */
    public Optional<IlliquidAsset> updateIlliquidAsset(Long portfolioId, Long assetId, IlliquidAssetDto illiquidAssetDto) {
        Optional<IlliquidAsset> existingAsset = illiquidAssetRepository.findByIdAndPortfolioId(assetId, portfolioId);

        if (existingAsset.isEmpty()) {
            return Optional.empty();
        }

        IlliquidAsset asset = existingAsset.get();
        asset.setName(illiquidAssetDto.getName());
        asset.setDescription(illiquidAssetDto.getDescription());
        asset.setEstimatedValue(illiquidAssetDto.getEstimatedValue());

        return Optional.of(illiquidAssetRepository.save(asset));
    }

    /**
     * Deletes an illiquid asset from the database.
     *
     * @param illiquidAsset The asset to delete
     */
    public void deleteIlliquidAsset(IlliquidAsset illiquidAsset) {
        illiquidAssetRepository.delete(illiquidAsset);
    }
}
