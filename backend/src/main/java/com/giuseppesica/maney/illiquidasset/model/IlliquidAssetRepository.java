package com.giuseppesica.maney.illiquidasset.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IlliquidAssetRepository extends JpaRepository<IlliquidAsset, Long> {
    List<IlliquidAsset> findByPortfolioId(Long portfolioId);
    Optional<IlliquidAsset> findByIdAndPortfolioId(Long id, Long portfolioId);
}
