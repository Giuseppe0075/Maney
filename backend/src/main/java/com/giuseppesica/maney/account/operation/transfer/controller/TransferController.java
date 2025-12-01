package com.giuseppesica.maney.account.operation.transfer.controller;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.transfer.model.Transfer;
import com.giuseppesica.maney.account.operation.transfer.model.TransferDto;
import com.giuseppesica.maney.account.operation.transfer.service.TransferService;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/portfolio/liquidity-accounts/transfers")
public class TransferController {

    private final UserService userService;
    private final TransferService transferService;

    @Autowired
    public TransferController(UserService userService, TransferService transferService) {
        this.userService = userService;
        this.transferService = transferService;
    }

    @GetMapping
    public ResponseEntity<List<TransferDto>> getAllTransfers(
            Authentication authentication
    ){
        User user = userService.UserFromAuthentication(authentication);
        List<Transfer> transfers = transferService.getTransfersByUserId(user);
        List<TransferDto> transferDtos =
                transfers.stream()
                        .map(TransferDto::new)
                        .toList();
        return ResponseEntity.ok(transferDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferDto> getTransferById(
            Authentication authentication,
            @PathVariable Long id
    ){
        User user = userService.UserFromAuthentication(authentication);
        Transfer transfer = transferService.getTransferByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Not Found Transfer with id: " + id));
        TransferDto transferDto = new TransferDto(transfer);
        return ResponseEntity.ok(transferDto);
    }
}
