package com.giuseppesica.maney.account.operation.transfer.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    @Query("SELECT t FROM Transfer t " +
            "WHERE t.fromAccount.portfolio.id = :portfolioId ")
    List<Transfer> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    @Query("SELECT  t FROM Transfer t " +
            "WHERE t.id = :id AND t.fromAccount.portfolio.id = :portfolioId")
    Optional<Transfer> findByIdAndPortfolioId(@Param("id") Long id, @Param("portfolioId") Long portfolioId);
}
