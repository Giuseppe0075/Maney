package com.giuseppesica.maney.account.operation.transfer.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransferDto {

    private Long id;

    @NotNull
    private Instant date;

    private String note;

    @NotNull
    private String fromAccountName;

    @NotNull
    private String toAccountName;

    @NotNull
    private BigDecimal amount;


    public TransferDto(Transfer transfer) {
        this.date = transfer.getDate();
        this.note = transfer.getNote();
        this.fromAccountName = transfer.getFromAccount().getName();
        this.toAccountName = transfer.getToAccount().getName();
        this.amount = transfer.getAmount();
    }
}
