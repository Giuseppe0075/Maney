package com.giuseppesica.maney.account.operation.cashmovement.model;

import com.giuseppesica.maney.utils.CashMovementType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class CashMovementDto {

    // Dati del movimento
    @NotNull
    private Instant date;

    private String note;

    // Identificatore opzionale della categoria (per ora manteniamo solo l'id)
    private Long categoryId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private CashMovementType type;

    // Informazioni minimali sull'account di liquidità
    // In request usiamo il nome per risolvere l'account dell'utente
    @NotNull
    private String liquidityAccountName;

    // In response possiamo esporre opzionalmente anche l'id dell'account
    private Long liquidityAccountId;

    public CashMovementDto(CashMovement cashMovement) {
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
