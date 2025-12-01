package com.giuseppesica.maney.account.operation.transfer.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransferDto {

    private Long id;

    @NotNull
    private Date date;

    private String note;

    @NotNull
    private Long fromAccountId;

    @NotNull
    private Long toAccountId;

    @NotNull
    private BigDecimal amount;


    public TransferDto(Transfer transfer) {
        this.date = Date.from(transfer.getDate());
        this.note = transfer.getNote();
        this.fromAccountId = transfer.getFromAccount().getId();
        this.toAccountId = transfer.getToAccount().getId();
        this.amount = transfer.getAmount();
    }
}
