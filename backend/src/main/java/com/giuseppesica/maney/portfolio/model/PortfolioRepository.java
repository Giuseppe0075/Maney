package com.giuseppesica.maney.portfolio.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for Portfolio entity.
 * Provides data access methods for user portfolios.
 */
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * Finds a portfolio by user ID.
     *
     * @param userId ID of the user
     * @return Optional containing the portfolio if found
     */
    Optional<Portfolio> findByUserId(Long userId);
}
