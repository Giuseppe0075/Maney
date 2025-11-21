package com.giuseppesica.maney.illiquidasset.dto;

import com.giuseppesica.maney.illiquidasset.model.IlliquidAsset;
import lombok.*;

/**
 * Data Transfer Object for IlliquidAsset.
 * Used to transfer illiquid asset data between layers without exposing the entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IlliquidAssetDto {

    /**
     * Unique identifier of the illiquid asset.
     */
    private Long id;

    /**
     * Name of the illiquid asset.
     */
    private String name;

    /**
     * Description of the illiquid asset.
     */
    private String description;

    /**
     * Estimated value of the asset.
     */
    private Float estimatedValue;

    /**
     * Constructor to create a DTO from an IlliquidAsset entity.
     *
     * @param illiquidAsset The entity to convert
     */
    public IlliquidAssetDto(IlliquidAsset illiquidAsset) {
        this.id = illiquidAsset.getId();
        this.name = illiquidAsset.getName();
        this.description = illiquidAsset.getDescription();
        this.estimatedValue = illiquidAsset.getEstimatedValue();
    }
}
