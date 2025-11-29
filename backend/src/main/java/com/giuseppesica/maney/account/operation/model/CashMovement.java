package com.giuseppesica.maney.account.operation.model;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.utils.CashMovementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class CashMovement extends Operation{

    @ManyToOne
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_cashmovement_category"))
    private Category category;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cashmovement_liquidityaccount"))
    private LiquidityAccount liquidityAccount;

    @NotNull
    @PositiveOrZero
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CashMovementType type;

}
