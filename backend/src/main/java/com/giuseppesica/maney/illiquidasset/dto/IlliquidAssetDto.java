package com.giuseppesica.maney.illiquidasset.dto;

import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IlliquidAssetDto {
    private Long id;
    private String name;
    private String description;
    private Float estimatedValue;

    public IlliquidAssetDto(IlliquidAsset illiquidAsset) {
        this.id = illiquidAsset.getId();
        this.name = illiquidAsset.getName();
        this.description = illiquidAsset.getDescription();
        this.estimatedValue = illiquidAsset.getEstimatedValue();
    }
}
