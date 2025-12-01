package com.giuseppesica.maney.account.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link LiquidityAccount} entities.
 *
 * <p>Provides standard CRUD operations plus custom query methods for
 * retrieving liquidity accounts by portfolio ownership.</p>
 *
 * <p><strong>Inherited Methods:</strong></p>
 * <ul>
 *   <li>{@code findById(Long)} - Find account by primary key</li>
 *   <li>{@code save(LiquidityAccount)} - Persist or update account</li>
 *   <li>{@code deleteById(Long)} - Remove account by ID</li>
 *   <li>{@code existsById(Long)} - Check if account exists</li>
 * </ul>
 *
 * @see LiquidityAccount
 * @see JpaRepository
 */
public interface LiquidityAccountRepository extends JpaRepository<LiquidityAccount, Long> {
    /**
     * Finds all liquidity accounts belonging to a specific portfolio.
     *
     * <p>Used to retrieve all accounts when displaying a user's portfolio
     * or validating account ownership.</p>
     *
     * <p>Query generated automatically by Spring Data based on method name:
     * {@code SELECT * FROM liquidity_account WHERE portfolio_id = ?}</p>
     *
     * @param portfolioId the portfolio ID to search for
     * @return list of accounts in the portfolio (may be empty)
     */
    List<LiquidityAccount> findByPortfolioId(Long portfolioId);
}
