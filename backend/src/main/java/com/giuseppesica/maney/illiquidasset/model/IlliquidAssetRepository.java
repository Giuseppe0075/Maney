package com.giuseppesica.maney.illiquidasset.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IlliquidAsset entity.
 * Provides data access methods for illiquid assets.
 */
public interface IlliquidAssetRepository extends JpaRepository<IlliquidAsset, Long> {

    /**
     * Finds all illiquid assets belonging to a specific portfolio.
     *
     * @param portfolioId ID of the portfolio
     * @return List of illiquid assets in the portfolio
     */
    List<IlliquidAsset> findByPortfolioId(Long portfolioId);

    /**
     * Finds an illiquid asset by its ID and portfolio ID.
     * Used to verify that an asset belongs to a specific portfolio.
     *
     * @param id ID of the asset
     * @param portfolioId ID of the portfolio
     * @return Optional containing the asset if found, empty otherwise
     */
    Optional<IlliquidAsset> findByIdAndPortfolioId(Long id, Long portfolioId);
}
