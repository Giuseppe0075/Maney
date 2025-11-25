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

@Entity
@DiscriminatorValue("LIQUID")
@NoArgsConstructor
@Getter
@Setter
public class LiquidityAccount extends Account{
    @NotNull
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Currency currency;

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
