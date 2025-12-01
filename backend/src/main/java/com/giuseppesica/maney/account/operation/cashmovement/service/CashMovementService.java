package com.giuseppesica.maney.account.operation.cashmovement.service;

import com.giuseppesica.maney.account.operation.cashmovement.model.CashMovement;
import com.giuseppesica.maney.account.operation.cashmovement.model.CashMovementRepository;
import com.giuseppesica.maney.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
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

