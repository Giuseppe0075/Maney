package com.giuseppesica.maney.account.operations.cashmovement.model;

import com.giuseppesica.maney.account.liquidityaccount.model.LiquidityAccount;
import com.giuseppesica.maney.account.operations.Operation;
import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.utils.CashMovementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity representing a single-account financial movement (income or outcome).
 *
 * <p>A cash movement records money entering (INCOME) or leaving (OUTCOME) a single
 * liquidity account. Unlike transfers which move funds between two accounts, cash
 * movements represent external transactions such as:</p>
 * <ul>
 *   <li><strong>Income:</strong> Salary, dividends, refunds, gifts received</li>
 *   <li><strong>Outcome:</strong> Purchases, bill payments, ATM withdrawals, fees</li>
 * </ul>
 *
 * <p><strong>Balance Impact:</strong></p>
 * <ul>
 *   <li>INCOME movements increase the account balance by the amount</li>
 *   <li>OUTCOME movements decrease the account balance by the amount</li>
 * </ul>
 *
 * <p><strong>Categorization:</strong> Each movement can optionally be assigned to a
 * {@link Category} for budgeting and reporting purposes. Categories are hierarchical
 * and help organize spending/income patterns.</p>
 *
 * <p><strong>Database Schema:</strong></p>
 * <ul>
 *   <li>Extends the Operation base table</li>
 *   <li>Foreign key to liquidity_account (required)</li>
 *   <li>Foreign key to category (optional)</li>
 *   <li>Amount stored as positive value; type determines add/subtract behavior</li>
 * </ul>
 *
 * @see Operation
 * @see LiquidityAccount
 * @see Category
 * @see CashMovementType
 * @see com.giuseppesica.maney.account.operations.cashmovement.service.CashMovementService
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashMovement extends Operation {

    /**
     * Optional category for organizing and reporting this movement.
     * Null is allowed for uncategorized transactions.
     * Categories can be hierarchical (parent-child relationships).
     */
    @ManyToOne
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_cashmovement_category"))
    private Category category;

    /**
     * The liquidity account affected by this movement.
     * Required - every cash movement must belong to exactly one account.
     * The account's balance is updated when the movement is created/deleted.
     */
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cashmovement_liquidityaccount"))
    private LiquidityAccount liquidityAccount;

    /**
     * The monetary amount of this movement.
     * Always stored as a positive value; the type field determines whether
     * this amount is added to or subtracted from the account balance.
     * Must be zero or positive.
     */
    @NotNull
    @PositiveOrZero
    private BigDecimal amount;

    /**
     * Type of movement: INCOME (adds to balance) or OUTCOME (subtracts from balance).
     * Stored as string in database for flexibility and readability.
     * Required for all cash movements.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private CashMovementType type;

}
