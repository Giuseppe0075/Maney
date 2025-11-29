package com.giuseppesica.maney.account.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiquidityAccountRepository extends JpaRepository<LiquidityAccount, Long> {
    List<LiquidityAccount> findByPortfolioId(Long portfolioId);
}
