package com.giuseppesica.maney.portfolio.dto;


import com.giuseppesica.maney.portfolio.model.Portfolio;
import lombok.Getter;

@Getter
public class PortfolioDto {
    private final Long id;

    public PortfolioDto(Portfolio portfolio){
        this.id = portfolio.getId();
    }

}
