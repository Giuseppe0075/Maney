package com.giuseppesica.maney.account.dto;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.utils.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class LiquidityAccountDto {

    @NotBlank
    @NotNull
    private String name;

    @NotBlank
    @NotNull
    private String institution;

    private Instant openedAt;

    private Instant closedAt;

    private String note;

    @NotNull
    private Currency currency;

    @NotNull
    private BigDecimal balance;

    @NotNull
    private Long portfolioId;

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
