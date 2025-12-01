package com.giuseppesica.maney.account.operations.cashmovement.model;

import com.giuseppesica.maney.utils.CashMovementType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object for cash movements (income/outcome operations).
 *
 * <p>This DTO represents a single financial movement affecting a liquidity account.
 * It serves dual purposes:</p>
 * <ul>
 *   <li><strong>Request payload:</strong> Client provides account name, amount, type, and optional details</li>
 *   <li><strong>Response payload:</strong> Includes complete movement data plus resolved account ID</li>
 * </ul>
 *
 * <p><strong>Usage in Requests:</strong></p>
 * <pre>
 * {
 *   "date": "2024-12-01T10:00:00Z",
 *   "amount": 150.00,
 *   "type": "INCOME",
 *   "liquidityAccountName": "Checking Account",
 *   "categoryId": 5,
 *   "note": "Freelance payment"
 * }
 * </pre>
 *
 * <p><strong>Usage in Responses:</strong></p>
 * <pre>
 * {
 *   "id": 42,
 *   "date": "2024-12-01T10:00:00Z",
 *   "amount": 150.00,
 *   "type": "INCOME",
 *   "liquidityAccountId": 10,
 *   "liquidityAccountName": "Checking Account",
 *   "categoryId": 5,
 *   "note": "Freelance payment"
 * }
 * </pre>
 *
 * @see CashMovement
 * @see CashMovementType
 */
@Getter
@Setter
@NoArgsConstructor
public class CashMovementDto {

    /**
     * Unique identifier of the cash movement.
     * Populated only in responses; ignored in create requests.
     */
    private Long id;

    /**
     * Timestamp when the movement occurred.
     * Required for all operations.
     */
    @NotNull
    private Instant date;

    /**
     * Optional descriptive note or memo for this movement.
     */
    private String note;

    /**
     * Optional reference to the category this movement belongs to.
     * Used for budgeting and reporting purposes.
     */
    private Long categoryId;

    /**
     * The monetary amount of this movement.
     * Always positive; the type field determines if it's added or subtracted.
     * Required for all operations.
     */
    @NotNull
    private BigDecimal amount;

    /**
     * Type of movement: INCOME (adds to balance) or OUTCOME (subtracts from balance).
     * Required for all operations.
     */
    @NotNull
    private CashMovementType type;

    /**
     * Name of the liquidity account affected by this movement.
     * Used in requests to resolve the account within the user's portfolio.
     * Required for create/update operations.
     */
    @NotNull
    private String liquidityAccountName;

    /**
     * Unique identifier of the liquidity account.
     * Populated only in responses after account resolution.
     */
    private Long liquidityAccountId;

    /**
     * Constructs a DTO from an existing CashMovement entity.
     *
     * <p>Populates all fields including:</p>
     * <ul>
     *   <li>Movement details (id, date, amount, type, note)</li>
     *   <li>Category reference if present</li>
     *   <li>Both account ID and name if liquidity account is set</li>
     * </ul>
     *
     * @param cashMovement the source entity to convert
     */
    public CashMovementDto(CashMovement cashMovement) {
        this.id = cashMovement.getId();
        this.date = cashMovement.getDate();
        this.note = cashMovement.getNote();
        this.categoryId = cashMovement.getCategory() != null ? cashMovement.getCategory().getId() : null;
        this.amount = cashMovement.getAmount();
        this.type = cashMovement.getType();
        if (cashMovement.getLiquidityAccount() != null) {
            this.liquidityAccountId = cashMovement.getLiquidityAccount().getId();
            this.liquidityAccountName = cashMovement.getLiquidityAccount().getName();
        }
    }
}
