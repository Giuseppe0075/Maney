package com.giuseppesica.maney.illiquidasset.model;

import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class IlliquidAsset {

    public IlliquidAsset(IlliquidAssetDto dto){
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.estimatedValue = dto.getEstimatedValue();
    }

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Float estimatedValue;

    @ManyToOne
    @JoinColumn(name="portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_illiquidasset_portfolio"))
    private Portfolio portfolio;
}
