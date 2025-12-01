package com.giuseppesica.maney.portfolio.dto;


import com.giuseppesica.maney.account.liquidityaccount.dto.LiquidityAccountDto;
import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import lombok.*;

import java.util.List;

/**
 * Data Transfer Object for Portfolio.
 * Used to transfer portfolio data between layers without exposing the entity.
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class PortfolioDto {

    /**
     * Unique identifier of the portfolio.
     */
    private final Long id;

    /**
     * List of illiquid assets in the portfolio.
     */
    private List<IlliquidAssetDto> illiquidAssets;

    /**
     * List of liquidity accounts in the portfolio.
     */
    private List<LiquidityAccountDto> liquidityAccounts;

    /**
     * Constructor to create a DTO from a Portfolio entity.
     * Converts all illiquid assets to DTOs.
     *
     * @param portfolio The entity to convert
     */
    public PortfolioDto(Portfolio portfolio){
        this.id = portfolio.getId();
    }
}
