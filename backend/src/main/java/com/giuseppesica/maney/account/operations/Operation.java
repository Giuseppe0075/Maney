package com.giuseppesica.maney.account.operations;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Abstract base entity for all financial operations in the system.
 *
 * <p>Represents any transaction or movement that occurs on accounts within the portfolio.
 * Concrete implementations include:</p>
 * <ul>
 *   <li>{@link com.giuseppesica.maney.account.operations.cashmovement.model.CashMovement}
 *       - Income or outcome affecting a single account</li>
 *   <li>{@link com.giuseppesica.maney.account.operations.transfer.model.Transfer}
 *       - Movement of funds between two accounts</li>
 * </ul>
 *
 * <p><strong>Common Attributes:</strong></p>
 * <ul>
 *   <li>Unique identifier for tracking and auditing</li>
 *   <li>Timestamp when the operation occurred</li>
 *   <li>Optional descriptive note for user reference</li>
 * </ul>
 *
 * <p><strong>Inheritance Strategy:</strong> Uses default JPA inheritance (SINGLE_TABLE).
 * Subclasses add their specific fields to represent different operation types.</p>
 *
 * <p><strong>Usage in Reporting:</strong> Operations can be queried across types to
 * build transaction histories, cash flow reports, and financial summaries.</p>
 *
 * @see com.giuseppesica.maney.account.operations.cashmovement.model.CashMovement
 * @see com.giuseppesica.maney.account.operations.transfer.model.Transfer
 */
@Entity
@Getter
@Setter
public abstract class Operation {
    /**
     * Unique identifier for this operation.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * Timestamp when this operation occurred or should be recorded.
     * Required for all operations - used for chronological ordering and reporting.
     */
    @NotNull
    private Instant date;

    /**
     * Optional user-provided description or memo for this operation.
     * Can contain details about the purpose, counterparty, or context.
     * Example: "Grocery shopping", "Salary payment", "Emergency fund transfer"
     */
    private String note;

    
}
