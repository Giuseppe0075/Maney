package com.giuseppesica.maney.account;

import com.giuseppesica.maney.account.liquidityaccount.model.LiquidityAccount;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Abstract base entity for all account types in the system.
 *
 * <p>Provides common fields and behavior shared by all account subtypes such as
 * {@link LiquidityAccount} and future account types (e.g., investment accounts,
 * illiquid assets).</p>
 *
 * <p><strong>Inheritance Strategy:</strong> Uses JPA JOINED table inheritance,
 * where each subclass has its own table with specific fields, joined to this
 * base table via the primary key.</p>
 *
 * <p><strong>Unique Constraints:</strong></p>
 * <ul>
 *   <li>Each account name must be unique within a portfolio</li>
 *   <li>Constraint name: {@code uk_account_portfolio_name}</li>
 * </ul>
 *
 * <p><strong>Lifecycle Management:</strong></p>
 * <ul>
 *   <li>createdAt and updatedAt are automatically managed via JPA callbacks</li>
 *   <li>Portfolio relationship is mandatory and validated on persist/update</li>
 * </ul>
 *
 * @see LiquidityAccount
 * @see Portfolio
 */
@Entity
@Table(
        name="account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_portfolio_name",
                        columnNames = {"portfolio_id", "name"}
                )
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class Account {
    /**
     * Unique identifier for this account.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable name for the account.
     * Must be unique within the owning portfolio.
     * Examples: "Checking Account", "Savings", "Emergency Fund"
     */
    @NotBlank
    @NotNull
    private String name;

    /**
     * Financial institution or service managing this account.
     * Required field to track where the account is held.
     * Examples: "Chase Bank", "PayPal", "Coinbase"
     */
    @NotBlank
    private String institution;

    /**
     * Optional timestamp when the account was opened.
     * Used for historical tracking and reporting.
     */
    private Instant openedAt;

    /**
     * Optional timestamp when the account was closed.
     * Null indicates the account is still active.
     * Closed accounts may be retained for historical data.
     */
    private Instant closedAt;

    /**
     * Optional free-text note about this account.
     * Can contain account numbers, purposes, or other metadata.
     */
    private String note;

    /**
     * Timestamp when this account entity was first created.
     * Automatically set by {@link #onCreate()} callback.
     */
    @NotNull
    private Instant createdAt;

    /**
     * Timestamp of the last update to this account.
     * Automatically updated by {@link #onUpdate()} callback.
     */
    @NotNull
    private Instant updatedAt;

    /**
     * The portfolio that owns this account.
     * Mandatory relationship - every account must belong to exactly one portfolio.
     * Cannot be null as enforced by database and validation.
     */
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    @NotNull
    private Portfolio portfolio;

    /**
     * JPA lifecycle callback invoked before persisting a new account.
     * Initializes createdAt and updatedAt timestamps to current time.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * JPA lifecycle callback invoked before updating an existing account.
     * Updates the updatedAt timestamp to current time.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

}



