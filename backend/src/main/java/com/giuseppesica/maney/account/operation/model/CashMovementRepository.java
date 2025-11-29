package com.giuseppesica.maney.account.operation.model;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {
    List<CashMovement> findByLiquidityAccountIn(List<LiquidityAccount> userLiquidityAccounts);
}
