package com.giuseppesica.maney.account.operation.service;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.model.LiquidityAccountRepository;
import com.giuseppesica.maney.account.operation.model.CashMovement;
import com.giuseppesica.maney.account.operation.model.CashMovementDto;
import com.giuseppesica.maney.account.operation.model.CashMovementRepository;
import com.giuseppesica.maney.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CashMovementService {

    private final LiquidityAccountRepository liquidityAccountRepository;
    private final CashMovementRepository cashMovementRepository;

    @Autowired
    public CashMovementService(LiquidityAccountRepository liquidityAccountRepository, CashMovementRepository cashMovementRepository) {
        this.liquidityAccountRepository = liquidityAccountRepository;
        this.cashMovementRepository = cashMovementRepository;
    }

    public List<CashMovement> getCashMovementsByUserId(User user) {
        List<LiquidityAccount> userLiquidityAccounts =
                liquidityAccountRepository.findByPortfolioId(user.getPortfolio().getId());
        return cashMovementRepository.findByLiquidityAccountIn(userLiquidityAccounts);
    }

    public CashMovement saveCashMovement(CashMovement cashMovement) {
        return cashMovementRepository.save(cashMovement);
    }

}
