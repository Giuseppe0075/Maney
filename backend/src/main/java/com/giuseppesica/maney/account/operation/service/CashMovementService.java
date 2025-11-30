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
import java.util.Optional;

@Service
public class CashMovementService {

    private final CashMovementRepository cashMovementRepository;

    @Autowired
    public CashMovementService(CashMovementRepository cashMovementRepository) {
        this.cashMovementRepository = cashMovementRepository;
    }

    public List<CashMovement> getCashMovementsByUserId(User user) {
        return cashMovementRepository.findByPortfolioId(user.getPortfolio().getId());
    }

    public CashMovement saveCashMovement(CashMovement cashMovement) {
        return cashMovementRepository.save(cashMovement);
    }

    public Optional<CashMovement> getCashMovementByIdAndUserId(Long id, User user) {
        return cashMovementRepository.findByIdAndPortfolioId(id, user.getPortfolio().getId());
    }

    public void deleteCashMovement(CashMovement cashMovement) {
        cashMovementRepository.delete(cashMovement);
    }
}

