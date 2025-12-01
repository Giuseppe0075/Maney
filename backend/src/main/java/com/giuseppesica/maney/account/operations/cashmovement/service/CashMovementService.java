package com.giuseppesica.maney.account.operations.cashmovement.service;

import com.giuseppesica.maney.account.liquidityaccount.service.LiquidityAccountService;
import com.giuseppesica.maney.account.operations.cashmovement.model.CashMovement;
import com.giuseppesica.maney.account.operations.cashmovement.model.CashMovementRepository;
import com.giuseppesica.maney.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for cash movement business logic.
 *
 * <p>Handles persistence and retrieval of cash movements (income/outcome operations).
 * This service focuses on data access and does not manage account balance updates -
 * that responsibility belongs to {@link LiquidityAccountService}.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Persist new cash movements</li>
 *   <li>Retrieve movements filtered by user/portfolio</li>
 *   <li>Delete existing movements</li>
 *   <li>Provide portfolio-scoped queries for authorization</li>
 * </ul>
 *
 * <p><strong>Not Responsible For:</strong></p>
 * <ul>
 *   <li>Updating account balances (handled by controller or dedicated service)</li>
 *   <li>Category validation (handled at entity/repository level)</li>
 *   <li>User authorization (handled by controller layer)</li>
 * </ul>
 *
 * @see CashMovement
 * @see CashMovementRepository
 * @see com.giuseppesica.maney.account.operations.cashmovement.control.CashMovementControl
 */
@Service
public class CashMovementService {

    private final CashMovementRepository cashMovementRepository;

    /**
     * Constructs the service with required repository dependency.
     *
     * @param cashMovementRepository repository for cash movement persistence
     */
    @Autowired
    public CashMovementService(CashMovementRepository cashMovementRepository) {
        this.cashMovementRepository = cashMovementRepository;
    }

    /**
     * Retrieves all cash movements for a specific user's portfolio.
     *
     * <p>Returns movements across all liquidity accounts owned by the user.
     * Useful for building transaction history views and financial reports.</p>
     *
     * @param user the authenticated user
     * @return list of cash movements (empty if no movements exist)
     */
    public List<CashMovement> getCashMovementsByUserId(User user) {
        return cashMovementRepository.findByPortfolioId(user.getPortfolio().getId());
    }

    /**
     * Persists a new or updated cash movement.
     *
     * <p><strong>Note:</strong> This method does NOT update the associated account balance.
     * Callers must separately invoke balance update logic on the liquidity account service.</p>
     *
     * @param cashMovement the movement to save (new or existing)
     * @return the persisted movement with generated ID if new
     */
    public CashMovement saveCashMovement(CashMovement cashMovement) {
        return cashMovementRepository.save(cashMovement);
    }

    /**
     * Finds a specific cash movement by ID with portfolio ownership validation.
     *
     * <p>Returns the movement only if it belongs to an account in the user's portfolio.
     * This ensures users cannot access movements from other portfolios.</p>
     *
     * @param id the movement ID to retrieve
     * @param user the authenticated user
     * @return Optional containing the movement if found and owned, empty otherwise
     */
    public Optional<CashMovement> getCashMovementByIdAndUserId(Long id, User user) {
        return cashMovementRepository.findByIdAndPortfolioId(id, user.getPortfolio().getId());
    }

    /**
     * Permanently removes a cash movement from the database.
     *
     * <p><strong>Note:</strong> This method does NOT reverse the balance impact.
     * Callers must separately update the account balance before calling this method.</p>
     *
     * @param cashMovement the movement to delete
     */
    public void deleteCashMovement(CashMovement cashMovement) {
        cashMovementRepository.delete(cashMovement);
    }
}

