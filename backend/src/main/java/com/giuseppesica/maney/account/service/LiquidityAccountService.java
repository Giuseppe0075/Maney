package com.giuseppesica.maney.account.service;

import com.giuseppesica.maney.account.controller.LiquidityAccountController;
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

/**
 * Service layer for liquidity account business logic.
 *
 * <p>Handles all operations related to liquidity accounts including:</p>
 * <ul>
 *   <li>CRUD operations (create, read, update, delete)</li>
 *   <li>Account lookups by portfolio and name</li>
 *   <li>Balance updates triggered by cash movements and transfers</li>
 *   <li>Portfolio ownership validation</li>
 * </ul>
 *
 * <p><strong>Transaction Management:</strong> Most operations are not explicitly
 * transactional at this level. Controllers using multiple service calls should
 * apply {@code @Transactional} to ensure atomicity.</p>
 *
 * <p><strong>Security:</strong> This service does not perform authorization checks.
 * Controllers must validate that users have access to the requested accounts/portfolios.</p>
 *
 * @see LiquidityAccount
 * @see LiquidityAccountRepository
 * @see LiquidityAccountController
 */
@Service
public class LiquidityAccountService {


    private final LiquidityAccountRepository liquidityAccountRepository;
    private final PortfolioRepository portfolioRepository;

    /**
     * Constructs the service with required repository dependencies.
     *
     * @param liquidityAccountRepository repository for account persistence
     * @param portfolioRepository repository for portfolio validation
     */
    public LiquidityAccountService(LiquidityAccountRepository liquidityAccountRepository, PortfolioRepository portfolioRepository) {
        this.liquidityAccountRepository = liquidityAccountRepository;
        this.portfolioRepository = portfolioRepository;
    }

    /**
     * Persists a new or existing liquidity account.
     *
     * <p>Validates that the associated portfolio exists before saving.
     * If the portfolio is not found or not properly set, throws NotFoundException.</p>
     *
     * <p><strong>Validation Steps:</strong></p>
     * <ol>
     *   <li>Check account has a portfolio reference</li>
     *   <li>Check portfolio has an ID</li>
     *   <li>Verify portfolio exists in database</li>
     *   <li>Attach persisted portfolio to account</li>
     * </ol>
     *
     * @param liquidityAccount the account to save (new or existing)
     * @return the persisted account with generated ID if new
     * @throws NotFoundException if portfolio is null, has no ID, or doesn't exist
     */
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

    /**
     * Retrieves all liquidity accounts for a specific portfolio.
     *
     * <p>Returns DTOs rather than entities to avoid exposing internal
     * structure to API consumers.</p>
     *
     * @param portfolioId the portfolio ID to query
     * @return list of account DTOs (empty if portfolio has no accounts)
     */
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
     * Updates an existing liquidity account with new data.
     *
     * <p>Replaces all mutable fields with values from the DTO. The portfolio
     * relationship is not updated by this method.</p>
     *
     * @param id the liquidity account ID to update
     * @param dto DTO containing new field values
     * @return the updated and persisted account
     * @throws NotFoundException if account with given ID doesn't exist
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
     * Permanently removes a liquidity account from the database.
     *
     * <p><strong>Warning:</strong> This operation cannot be undone. All transaction
     * history referencing this account may become orphaned.</p>
     *
     * @param id the liquidity account ID to delete
     * @throws NotFoundException if account doesn't exist
     */
    public void deleteLiquidityAccount(Long id) {
        if (!liquidityAccountRepository.existsById(id)) {
            throw new NotFoundException("Liquidity account not found");
        }
        liquidityAccountRepository.deleteById(id);
    }

    /**
     * Finds an account by portfolio ID and account name.
     *
     * <p>Used primarily by transfer and cash movement operations to resolve
     * account references from user-friendly names rather than IDs.</p>
     *
     * <p>This method performs an in-memory filter rather than a database query.
     * For large portfolios, consider adding a custom repository query.</p>
     *
     * @param portfolioId the portfolio to search within
     * @param name exact account name to match (case-sensitive)
     * @return Optional containing the account if found, empty otherwise
     */
    public Optional<LiquidityAccount> getLiquidityAccountByPortfolioIdAndName(Long portfolioId, String name) {
        return liquidityAccountRepository.findByPortfolioId(portfolioId)
                .stream()
                .filter(account -> account.getName().equals(name))
                .findFirst();
    }

    /**
     * Updates an account balance based on a cash movement.
     *
     * <p>This method is called by cash movement operations to apply income/outcome
     * effects to the account balance. The account object is modified in-place and
     * then persisted.</p>
     *
     * <p><strong>Balance Changes:</strong></p>
     * <ul>
     *   <li>INCOME: balance = balance + amount</li>
     *   <li>OUTCOME: balance = balance - amount</li>
     * </ul>
     *
     * <p><strong>Note:</strong> The account parameter must be a managed entity
     * or detached entity with a valid ID. For best results, call within a
     * transactional context.</p>
     *
     * @param liquidityAccount the account to update (modified in-place)
     * @param amount the movement amount (must be positive)
     * @param type movement type (INCOME or OUTCOME)
     * @throws IllegalArgumentException if type is null or invalid
     */
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
