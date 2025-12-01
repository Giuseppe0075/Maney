package com.giuseppesica.maney.account.operation.transfer.model;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.Operation;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity representing a transfer of funds between two liquidity accounts.
 *
 * <p>A transfer moves money from one account (source/from) to another (destination/to)
 * within the same portfolio. Unlike cash movements which affect a single account,
 * transfers maintain balance consistency by debiting one account and crediting another
 * with the same amount.</p>
 *
 * <p><strong>Balance Impact:</strong></p>
 * <ul>
 *   <li>From Account: balance -= amount (debited)</li>
 *   <li>To Account: balance += amount (credited)</li>
 *   <li>Net portfolio balance: unchanged (internal movement)</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Moving funds between checking and savings</li>
 *   <li>Consolidating multiple accounts</li>
 *   <li>Allocating funds to specific purpose accounts</li>
 *   <li>Rebalancing account distributions</li>
 * </ul>
 *
 * <p><strong>Constraints:</strong></p>
 * <ul>
 *   <li>Both accounts must be non-null and exist</li>
 *   <li>Amount must be positive (direction determined by from→to relationship)</li>
 *   <li>Typically both accounts belong to same portfolio (enforced by controller)</li>
 * </ul>
 *
 * <p><strong>Database Schema:</strong></p>
 * <ul>
 *   <li>Extends the Operation base table</li>
 *   <li>Foreign keys to both source and destination liquidity accounts</li>
 *   <li>Amount stored as positive value</li>
 * </ul>
 *
 * @see Operation
 * @see LiquidityAccount
 * @see com.giuseppesica.maney.account.operation.transfer.service.TransferService
 * @see com.giuseppesica.maney.account.operation.transfer.controller.TransferController
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transfer extends Operation {

    /**
     * The source account from which funds are transferred (debited).
     * Required - every transfer must have a source account.
     * Balance is decreased by the transfer amount.
     */
    @ManyToOne
    @JoinColumn(name = "from_account_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_transfer_from_liquidityaccount"))
    @NotNull
    private LiquidityAccount fromAccount;

    /**
     * The destination account to which funds are transferred (credited).
     * Required - every transfer must have a destination account.
     * Balance is increased by the transfer amount.
     */
    @ManyToOne
    @JoinColumn(name = "to_account_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_transfer_to_liquidityaccount"))
    @NotNull
    private LiquidityAccount toAccount;

    /**
     * The amount of funds being transferred.
     * Must be positive - the from→to direction determines the flow.
     * Applied as subtraction from source and addition to destination.
     */
    @NotNull
    private BigDecimal amount;
}
