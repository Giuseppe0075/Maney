package com.giuseppesica.maney.account.operation.transfer.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object for transfers between liquidity accounts.
 *
 * <p>Represents the data needed to create or display a transfer operation.
 * Uses account names rather than IDs for user-friendly API interaction.</p>
 *
 * <p><strong>Request Example (Create/Update):</strong></p>
 * <pre>
 * {
 *   "date": "2024-12-01T15:30:00Z",
 *   "fromAccountName": "Checking",
 *   "toAccountName": "Savings",
 *   "amount": 500.00,
 *   "note": "Monthly savings allocation"
 * }
 * </pre>
 *
 * <p><strong>Response Example:</strong></p>
 * <pre>
 * {
 *   "id": 123,
 *   "date": "2024-12-01T15:30:00Z",
 *   "fromAccountName": "Checking",
 *   "toAccountName": "Savings",
 *   "amount": 500.00,
 *   "note": "Monthly savings allocation"
 * }
 * </pre>
 *
 * <p><strong>Design Rationale:</strong> Using account names instead of IDs makes
 * the API more intuitive for clients. The server resolves names to entities within
 * the user's portfolio during processing.</p>
 *
 * @see Transfer
 * @see com.giuseppesica.maney.account.model.LiquidityAccount
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransferDto {

    /**
     * Unique identifier of the transfer.
     * Populated in responses; ignored in create requests.
     */
    private Long id;

    /**
     * Timestamp when the transfer occurred or should be recorded.
     * Required for all operations.
     */
    @NotNull
    private Instant date;

    /**
     * Optional descriptive note about the transfer.
     * Examples: "Monthly savings", "Emergency fund allocation"
     */
    private String note;

    /**
     * Name of the source account (funds are debited from here).
     * Must exactly match an account name in the user's portfolio.
     * Required for create/update operations.
     */
    @NotNull
    private String fromAccountName;

    /**
     * Name of the destination account (funds are credited here).
     * Must exactly match an account name in the user's portfolio.
     * Required for create/update operations.
     */
    @NotNull
    private String toAccountName;

    /**
     * The amount to transfer.
     * Must be positive - direction is from→to.
     * Required for all operations.
     */
    @NotNull
    private BigDecimal amount;


    /**
     * Constructs a DTO from an existing Transfer entity.
     *
     * <p>Converts the entity to a transferable format suitable for API responses.
     * Extracts account names from the entity's account relationships.</p>
     *
     * @param transfer the source entity to convert
     */
    public TransferDto(Transfer transfer) {
        this.date = transfer.getDate();
        this.note = transfer.getNote();
        this.fromAccountName = transfer.getFromAccount().getName();
        this.toAccountName = transfer.getToAccount().getName();
        this.amount = transfer.getAmount();
    }
}
