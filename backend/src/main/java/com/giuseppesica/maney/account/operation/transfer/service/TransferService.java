package com.giuseppesica.maney.account.operation.transfer.service;

import com.giuseppesica.maney.account.operation.transfer.model.Transfer;
import com.giuseppesica.maney.account.operation.transfer.model.TransferRepository;
import com.giuseppesica.maney.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransferService {
    private final TransferRepository transferRepository;

    @Autowired
    public TransferService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public List<Transfer> getTransfersByUserId(User user) {
        return transferRepository.findByPortfolioId(user.getPortfolio().getId());
    }

    public Optional<Transfer> getTransferByIdAndUserId(Long id, User user) {
        return transferRepository.findByIdAndPortfolioId(id, user.getPortfolio().getId());
    }
}
