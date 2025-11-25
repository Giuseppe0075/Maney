package com.giuseppesica.maney.account.service;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.model.LiquidityAccountRepository;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.model.PortfolioRepository;
import org.springframework.stereotype.Service;

@Service
public class LiquidityAccountService {


    private final LiquidityAccountRepository liquidityAccountRepository;
    private final PortfolioRepository portfolioRepository;

    public LiquidityAccountService(LiquidityAccountRepository liquidityAccountRepository, PortfolioRepository portfolioRepository) {
        this.liquidityAccountRepository = liquidityAccountRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public LiquidityAccount saveLiquidityAccount(LiquidityAccountDto dto) {
        Portfolio portfolio = portfolioRepository.findById(dto.getPortfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio Not Found"));

        LiquidityAccount liquidityAccount = new LiquidityAccount(dto);
        liquidityAccount.setPortfolio(portfolio);
        return liquidityAccountRepository.save(liquidityAccount);
    }
}
