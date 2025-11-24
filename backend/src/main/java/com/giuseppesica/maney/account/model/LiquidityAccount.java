package com.giuseppesica.maney.account.model;

import com.giuseppesica.maney.utils.Currency;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class LiquidityAccount extends Account{
    @NotNull
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Currency currency;
}
