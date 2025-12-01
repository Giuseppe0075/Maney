package com.giuseppesica.maney.account.operations.transfer.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Transfer} entities.
 *
 * <p>Provides standard CRUD operations plus custom queries for retrieving
 * transfers filtered by portfolio ownership. Queries use the source (from)
 * account's portfolio to determine ownership.</p>
 *
 * <p><strong>Portfolio Scoping:</strong> All custom queries join through
 * {@code fromAccount.portfolio} to ensure users can only access transfers
 * originating from their portfolio. This assumes both accounts in a transfer
 * belong to the same portfolio (enforced by controller logic).</p>
 *
 * @see Transfer
 * @see JpaRepository
 */
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    /**
     * Finds all transfers where the source account belongs to a specific portfolio.
     *
     * <p>Used to retrieve a user's complete transfer history for reporting
     * and transaction views.</p>
     *
     * <p><strong>Query Logic:</strong> Joins Transfer → fromAccount → Portfolio
     * to filter by portfolio ownership.</p>
     *
     * @param portfolioId the portfolio ID to search within
     * @return list of transfers (may be empty)
     */
    @Query("SELECT t FROM Transfer t " +
            "WHERE t.fromAccount.portfolio.id = :portfolioId ")
    List<Transfer> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Finds a specific transfer by ID, validating portfolio ownership.
     *
     * <p>Used for authorization checks - ensures the requested transfer's
     * source account belongs to the user's portfolio before allowing access.</p>
     *
     * <p><strong>Query Logic:</strong> Combines primary key lookup with portfolio
     * filter to enforce ownership validation.</p>
     *
     * @param id the transfer ID to retrieve
     * @param portfolioId the portfolio ID to validate ownership
     * @return Optional containing the transfer if found and owned, empty otherwise
     */
    @Query("SELECT  t FROM Transfer t " +
            "WHERE t.id = :id AND t.fromAccount.portfolio.id = :portfolioId")
    Optional<Transfer> findByIdAndPortfolioId(@Param("id") Long id, @Param("portfolioId") Long portfolioId);
}
