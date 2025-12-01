package com.giuseppesica.maney.account.model;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.utils.Currency;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity representing a liquid financial account (cash or checking account).
 *
 * <p>A liquidity account holds readily available funds that can be used for
 * transactions such as income/outcome operations and transfers. This is the most
 * common account type in the system.</p>
 *
 * <p><strong>Discriminator Value:</strong> LIQUID - identifies this subclass in
 * the account hierarchy.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Tracks current balance in a specific currency</li>
 *   <li>Balance updated automatically by cash movements and transfers</li>
 *   <li>Supports multi-currency portfolios (each account has one currency)</li>
 *   <li>Inherits common account fields (name, institution, dates) from {@link Account}</li>
 * </ul>
 *
 * <p><strong>Database Structure:</strong></p>
 * <ul>
 *   <li>Extends account table using JOINED inheritance</li>
 *   <li>Has its own table for liquidity-specific fields (balance, currency)</li>
 *   <li>Primary key is shared with the parent account table</li>
 * </ul>
 *
 * <p><strong>Balance Management:</strong></p>
 * <ul>
 *   <li>Increased by INCOME cash movements</li>
 *   <li>Decreased by OUTCOME cash movements</li>
 *   <li>Debited when used as source in transfers</li>
 *   <li>Credited when used as destination in transfers</li>
 * </ul>
 *
 * @see Account
 * @see Currency
 * @see com.giuseppesica.maney.account.operation.cashmovement.model.CashMovement
 * @see com.giuseppesica.maney.account.operation.transfer.model.Transfer
 */
@Entity
@DiscriminatorValue("LIQUID")
@NoArgsConstructor
@Getter
@Setter
public class LiquidityAccount extends Account{
    /**
     * Current balance of the account.
     * Updated automatically by financial operations.
     * Can be negative if overdrafts are allowed.
     */
    @NotNull
    private BigDecimal balance;

    /**
     * The currency in which this account operates.
     * All transactions affecting this account must use this currency.
     * Stored as string in database for flexibility.
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    private Currency currency;

    /**
     * Constructs a LiquidityAccount from a DTO.
     *
     * <p>Initializes all inherited Account fields plus liquidity-specific
     * fields (balance and currency). The portfolio relationship must be
     * set separately after construction.</p>
     *
     * @param dto DTO containing initial account data
     */
    public LiquidityAccount(LiquidityAccountDto dto){
        this.setName(dto.getName());
        this.setInstitution(dto.getInstitution());
        this.setOpenedAt(dto.getOpenedAt());
        this.setClosedAt(dto.getClosedAt());
        this.setNote(dto.getNote());
        this.currency = dto.getCurrency();
        this.balance = dto.getBalance();
    }
}
