package com.giuseppesica.maney.account.operation.cashmovement.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {

    @Query("SELECT cm FROM CashMovement cm " +
            "WHERE cm.liquidityAccount.portfolio.id = :portfolioId")
    List<CashMovement> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    @Query("SELECT cm FROM CashMovement cm " +
            "WHERE cm.id = :id AND cm.liquidityAccount.portfolio.id = :portfolioId")
    Optional<CashMovement> findByIdAndPortfolioId(@Param("id") Long id,
                                                  @Param("portfolioId") Long portfolioId);
}
