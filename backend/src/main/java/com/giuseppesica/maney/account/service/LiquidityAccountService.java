package com.giuseppesica.maney.account.service;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.model.LiquidityAccountRepository;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.model.PortfolioRepository;
import com.giuseppesica.maney.utils.CashMovementType;
import com.giuseppesica.maney.security.NotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public LiquidityAccount saveLiquidityAccount(LiquidityAccount liquidityAccount) {
        Portfolio portfolio = Optional.ofNullable(liquidityAccount.getPortfolio())
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        Long portfolioId = Optional.of(portfolio.getId())
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        Portfolio persistedPortfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        liquidityAccount.setPortfolio(persistedPortfolio);
        return liquidityAccountRepository.save(liquidityAccount);
    }

    public List<LiquidityAccountDto> getLiquidityAccounts(Long portfolioId) {
        List<LiquidityAccount> accounts = liquidityAccountRepository.findByPortfolioId(portfolioId);
        return accounts.stream().map(LiquidityAccountDto::new).toList();
    }

    /**
     * Retrieves a liquidity account by its ID.
     *
     * @param id the liquidity account ID
     * @return Optional containing the liquidity account if found
     */
    public Optional<LiquidityAccount> getLiquidityAccountById(Long id) {
        return liquidityAccountRepository.findById(id);
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
                .orElseThrow(() -> new NotFoundException("Liquidity account not found"));

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
            throw new NotFoundException("Liquidity account not found");
        }
        liquidityAccountRepository.deleteById(id);
    }

    public Optional<LiquidityAccount> getLiquidityAccountByPortfolioIdAndName(Long portfolioId, String name) {
        return liquidityAccountRepository.findByPortfolioId(portfolioId)
                .stream()
                .filter(account -> account.getName().equals(name))
                .findFirst();
    }

    public void updateLiquidityAccount(LiquidityAccount liquidityAccount, BigDecimal amount, CashMovementType type) {
        BigDecimal updatedBalance;
        if (type == CashMovementType.INCOME) {
            updatedBalance = liquidityAccount.getBalance().add(amount);
        } else if (type == CashMovementType.OUTCOME) {
            updatedBalance = liquidityAccount.getBalance().subtract(amount);
        } else {
            throw new IllegalArgumentException("Invalid Cash Movement Type");
        }
        liquidityAccount.setBalance(updatedBalance);
        liquidityAccountRepository.save(liquidityAccount);
    }
}
