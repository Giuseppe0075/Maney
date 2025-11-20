package com.giuseppesica.maney.portfolio.dto;


import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import lombok.*;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class PortfolioDto {
    private final Long id;
    private List<IlliquidAssetDto> illiquidAssets;

    public PortfolioDto(Portfolio portfolio){
        this.id = portfolio.getId();
    }
}
