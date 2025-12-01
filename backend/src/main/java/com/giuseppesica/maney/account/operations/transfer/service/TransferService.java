package com.giuseppesica.maney.account.operations.transfer.service;

import com.giuseppesica.maney.account.liquidityaccount.service.LiquidityAccountService;
import com.giuseppesica.maney.account.operations.transfer.model.Transfer;
import com.giuseppesica.maney.account.operations.transfer.model.TransferRepository;
import com.giuseppesica.maney.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for transfer business logic.
 *
 * <p>Handles persistence and retrieval of transfers between liquidity accounts.
 * This service focuses on data access and does not manage account balance updates -
 * that responsibility belongs to {@link LiquidityAccountService}
 * and is coordinated by {@link com.giuseppesica.maney.account.operations.transfer.controller.TransferController}.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Persist new transfers</li>
 *   <li>Retrieve transfers filtered by user/portfolio</li>
 *   <li>Delete existing transfers</li>
 *   <li>Provide portfolio-scoped queries for authorization</li>
 * </ul>
 *
 * <p><strong>Not Responsible For:</strong></p>
 * <ul>
 *   <li>Updating account balances (handled by controller using LiquidityAccountService)</li>
 *   <li>Account name resolution (handled by controller)</li>
 *   <li>User authorization (handled by controller layer)</li>
 *   <li>Transaction management (handled by controller with @Transactional)</li>
 * </ul>
 *
 * @see Transfer
 * @see TransferRepository
 * @see com.giuseppesica.maney.account.operations.transfer.controller.TransferController
 */
@Service
public class TransferService {
    private final TransferRepository transferRepository;

    /**
     * Constructs the service with required repository dependency.
     *
     * @param transferRepository repository for transfer persistence
     */
    @Autowired
    public TransferService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    /**
     * Retrieves all transfers for a specific user's portfolio.
     *
     * <p>Returns transfers where the source account belongs to the user's portfolio.
     * Useful for building transfer history views and financial reports.</p>
     *
     * @param user the authenticated user
     * @return list of transfers (empty if no transfers exist)
     */
    public List<Transfer> getTransfersByUserId(User user) {
        return transferRepository.findByPortfolioId(user.getPortfolio().getId());
    }

    /**
     * Finds a specific transfer by ID with portfolio ownership validation.
     *
     * <p>Returns the transfer only if its source account belongs to the user's portfolio.
     * This ensures users cannot access transfers from other portfolios.</p>
     *
     * @param id the transfer ID to retrieve
     * @param user the authenticated user
     * @return Optional containing the transfer if found and owned, empty otherwise
     */
    public Optional<Transfer> getTransferByIdAndUserId(Long id, User user) {
        return transferRepository.findByIdAndPortfolioId(id, user.getPortfolio().getId());
    }

    /**
     * Persists a new or updated transfer.
     *
     * <p><strong>Note:</strong> This method does NOT update the associated account balances.
     * Callers must separately invoke balance update logic on the liquidity account service
     * for both the source and destination accounts.</p>
     *
     * @param transfer the transfer to save (new or existing)
     * @return the persisted transfer with generated ID if new
     */
    public Transfer saveTransfer(Transfer transfer) {
        return transferRepository.save(transfer);
    }

    /**
     * Permanently removes a transfer from the database.
     *
     * <p><strong>Note:</strong> This method does NOT reverse the balance impact.
     * Callers must separately update both account balances before calling this method
     * to maintain data integrity.</p>
     *
     * @param id the transfer ID to delete
     */
    public void deleteTransferById(Long id) {
        transferRepository.deleteById(id);
    }
}
