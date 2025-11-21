package com.giuseppesica.maney.illiquidasset.service;

import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import com.giuseppesica.maney.illiquidasset.model.IlliquidAssetRepository;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IlliquidAssetService {

    private final IlliquidAssetRepository illiquidAssetRepository;

    @Autowired
    public IlliquidAssetService(IlliquidAssetRepository illiquidAssetRepository) {
        this.illiquidAssetRepository = illiquidAssetRepository;
    }

    public List<IlliquidAssetDto> getIlliquidAssets(Long portfolioId) {
        List<IlliquidAsset> illiquidAssets = illiquidAssetRepository.findByPortfolioId(portfolioId);
        return illiquidAssets.stream()
                .map(IlliquidAssetDto::new)
                .toList();
    }

    public Optional<IlliquidAsset> getIlliquidAssetById(Long portfolioId, Long assetId) {
        return illiquidAssetRepository.findByIdAndPortfolioId(assetId, portfolioId);
    }

    public IlliquidAsset createIlliquidAsset(IlliquidAssetDto illiquidAssetDto, Portfolio portfolio) {
        IlliquidAsset illiquidAsset = new IlliquidAsset(illiquidAssetDto);
        illiquidAsset.setPortfolio(portfolio);
        return illiquidAssetRepository.save(illiquidAsset);
    }

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

    public void deleteIlliquidAsset(IlliquidAsset illiquidAsset) {
        illiquidAssetRepository.delete(illiquidAsset);
    }
}
