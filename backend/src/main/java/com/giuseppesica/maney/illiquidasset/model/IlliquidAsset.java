package com.giuseppesica.maney.illiquidasset.model;

import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an illiquid asset in the system.
 * Illiquid assets are assets that cannot be easily converted to cash,
 * such as real estate, art, or private equity.
 */
@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class IlliquidAsset {

    /**
     * Constructor to create an IlliquidAsset from a DTO.
     *
     * @param dto DTO containing asset information
     */
    public IlliquidAsset(IlliquidAssetDto dto){
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.estimatedValue = dto.getEstimatedValue();
    }

    /**
     * Unique identifier for the illiquid asset.
     */
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the illiquid asset.
     */
    @NotNull
    @NotBlank
    private String name;

    /**
     * Description of the illiquid asset.
     */
    private String description;

    /**
     * Estimated value of the asset in the user's currency.
     */
    @NotNull
    private Float estimatedValue;

    /**
     * Portfolio that owns this asset.
     * Many-to-one relationship: many assets can belong to one portfolio.
     */
    @ManyToOne
    @JoinColumn(name="portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_illiquidasset_portfolio"))
    private Portfolio portfolio;
}
