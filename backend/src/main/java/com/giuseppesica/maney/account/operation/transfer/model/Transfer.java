package com.giuseppesica.maney.account.operation.transfer.model;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.Operation;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transfer extends Operation {

    @ManyToOne
    @JoinColumn(name = "from_account_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_transfer_from_liquidityaccount"))
    @NotNull
    private LiquidityAccount fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_transfer_to_liquidityaccount"))
    @NotNull
    private LiquidityAccount toAccount;

    @NotNull
    private BigDecimal amount;
}
