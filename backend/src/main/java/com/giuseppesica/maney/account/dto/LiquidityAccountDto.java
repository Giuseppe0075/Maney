package com.giuseppesica.maney.account.dto;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.utils.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object for liquidity accounts.
 *
 * <p>Represents a cash or checking account that holds liquid funds. This DTO is used
 * for both request and response payloads in liquidity account management endpoints.</p>
 *
 * <p><strong>Request Example (Create):</strong></p>
 * <pre>
 * {
 *   "name": "Checking Account",
 *   "institution": "Bank of America",
 *   "currency": "USD",
 *   "balance": 5000.00,
 *   "portfolioId": 1,
 *   "openedAt": "2024-01-15T10:00:00Z",
 *   "note": "Primary checking account"
 * }
 * </pre>
 *
 * <p><strong>Validation Rules:</strong></p>
 * <ul>
 *   <li>name - Required, non-blank identifier for the account</li>
 *   <li>institution - Required, non-blank bank or institution name</li>
 *   <li>currency - Required, must be a valid Currency enum value</li>
 *   <li>balance - Required, current account balance</li>
 *   <li>portfolioId - Required, must reference an existing portfolio</li>
 *   <li>openedAt, closedAt, note - Optional metadata</li>
 * </ul>
 *
 * @see LiquidityAccount
 * @see Currency
 */
@Getter
@Setter
@NoArgsConstructor
public class LiquidityAccountDto {

    /**
     * The account name or identifier.
     * Must be unique within the portfolio.
     */
    @NotBlank
    @NotNull
    private String name;

    /**
     * The financial institution managing this account.
     * Example: "Chase Bank", "Wells Fargo"
     */
    @NotBlank
    @NotNull
    private String institution;

    /**
     * Date when the account was opened.
     * Optional field for record-keeping.
     */
    private Instant openedAt;

    /**
     * Date when the account was closed, if applicable.
     * Null indicates the account is still active.
     */
    private Instant closedAt;

    /**
     * Optional descriptive note about this account.
     * Can include account number suffix, purpose, or other details.
     */
    private String note;

    /**
     * The currency in which this account operates.
     * All transactions and balances use this currency.
     */
    @NotNull
    private Currency currency;

    /**
     * Current balance of the account.
     * Updated automatically by income/outcome operations and transfers.
     */
    @NotNull
    private BigDecimal balance;

    /**
     * Reference to the portfolio that owns this account.
     * Used to validate user access and organize accounts.
     */
    @NotNull
    private Long portfolioId;

    /**
     * Constructs a DTO from an existing LiquidityAccount entity.
     *
     * <p>Converts the entity to a transferable format suitable for API responses.
     * All account details and the portfolio reference are copied.</p>
     *
     * @param liquidityAccount the source entity to convert
     */
    public LiquidityAccountDto(LiquidityAccount liquidityAccount) {
        this.name = liquidityAccount.getName();
        this.institution = liquidityAccount.getInstitution();
        this.openedAt = liquidityAccount.getOpenedAt();
        this.closedAt = liquidityAccount.getClosedAt();
        this.note = liquidityAccount.getNote();
        this.currency = liquidityAccount.getCurrency();
        this.balance = liquidityAccount.getBalance();
        this.portfolioId = liquidityAccount.getPortfolio().getId();
    }
}
