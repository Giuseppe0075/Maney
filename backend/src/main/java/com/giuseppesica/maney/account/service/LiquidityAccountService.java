package com.giuseppesica.maney.account.service;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.model.LiquidityAccountRepository;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.model.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public List<LiquidityAccountDto> getLiquidityAccounts(Long portfolioId) {
        List<LiquidityAccount> accounts = liquidityAccountRepository.findByPortfolioId(portfolioId).stream().map(account -> (LiquidityAccount) account).toList();
        return accounts.stream().map(LiquidityAccountDto::new).toList();
    }

    /**
     * Retrieves a liquidity account by its ID.
     *
     * @param id the liquidity account ID
     * @return Optional containing the liquidity account if found
     */
    public Optional<LiquidityAccount> getLiquidityAccountById(Long id) {
        return liquidityAccountRepository.findById(id)
                .map(account -> (LiquidityAccount) account);
    }

    /**
     * Updates an existing liquidity account.
     *
     * @param id the liquidity account ID
     * @param dto the updated liquidity account data
     * @return the updated liquidity account
     * @throws IllegalArgumentException if account or portfolio is not found
     */
    public LiquidityAccount updateLiquidityAccount(Long id, LiquidityAccountDto dto) {
        LiquidityAccount account = liquidityAccountRepository.findById(id)
                .map(acc -> (LiquidityAccount) acc)
                .orElseThrow(() -> new IllegalArgumentException("Liquidity Account Not Found"));

        // Update fields from Account base class
        account.setName(dto.getName());
        account.setInstitution(dto.getInstitution());
        account.setOpenedAt(dto.getOpenedAt());
        account.setClosedAt(dto.getClosedAt());
        account.setNote(dto.getNote());

        // Update fields specific to LiquidityAccount
        account.setBalance(dto.getBalance());
        account.setCurrency(dto.getCurrency());

        return liquidityAccountRepository.save(account);
    }

    /**
     * Deletes a liquidity account by its ID.
     *
     * @param id the liquidity account ID
     * @throws IllegalArgumentException if account is not found
     */
    public void deleteLiquidityAccount(Long id) {
        if (!liquidityAccountRepository.existsById(id)) {
            throw new IllegalArgumentException("Liquidity Account Not Found");
        }
        liquidityAccountRepository.deleteById(id);
    }
}
