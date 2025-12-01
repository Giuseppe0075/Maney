package com.giuseppesica.maney.account.operation.cashmovement.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link CashMovement} entities.
 *
 * <p>Provides standard CRUD operations plus custom queries for retrieving
 * cash movements filtered by portfolio ownership.</p>
 *
 * <p><strong>Custom Queries:</strong></p>
 * <ul>
 *   <li>Portfolio-scoped listing for displaying user's transaction history</li>
 *   <li>Portfolio-scoped single lookup for authorization checks</li>
 * </ul>
 *
 * <p><strong>Security Note:</strong> All custom queries join through the account's
 * portfolio relationship to ensure users can only access their own movements.</p>
 *
 * @see CashMovement
 * @see JpaRepository
 */
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {

    /**
     * Finds all cash movements for accounts within a specific portfolio.
     *
     * <p>Retrieves movements across all liquidity accounts in the portfolio,
     * enabling transaction history views and financial reports.</p>
     *
     * <p><strong>Query Logic:</strong> Joins CashMovement → LiquidityAccount → Portfolio
     * to filter by portfolio ownership.</p>
     *
     * @param portfolioId the portfolio ID to search within
     * @return list of cash movements (may be empty)
     */
    @Query("SELECT cm FROM CashMovement cm " +
            "WHERE cm.liquidityAccount.portfolio.id = :portfolioId")
    List<CashMovement> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Finds a specific cash movement by ID, validating portfolio ownership.
     *
     * <p>Used for authorization checks - ensures the requested movement belongs
     * to an account in the user's portfolio before allowing access.</p>
     *
     * <p><strong>Query Logic:</strong> Combines primary key lookup with portfolio
     * filter to enforce ownership validation.</p>
     *
     * @param id the cash movement ID to retrieve
     * @param portfolioId the portfolio ID to validate ownership
     * @return Optional containing the movement if found and owned, empty otherwise
     */
    @Query("SELECT cm FROM CashMovement cm " +
            "WHERE cm.id = :id AND cm.liquidityAccount.portfolio.id = :portfolioId")
    Optional<CashMovement> findByIdAndPortfolioId(@Param("id") Long id,
                                                  @Param("portfolioId") Long portfolioId);
}
