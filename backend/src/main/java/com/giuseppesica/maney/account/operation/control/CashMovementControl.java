package com.giuseppesica.maney.account.operation.control;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.model.CashMovement;
import com.giuseppesica.maney.account.operation.model.CashMovementDto;
import com.giuseppesica.maney.account.operation.service.CashMovementService;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user/cash-movements")
public class CashMovementControl {

    private final UserService userService;
    private final CashMovementService cashMovementService;
    private final LiquidityAccountService liquidityAccountService;

    @Autowired
    public CashMovementControl(UserService userService, CashMovementService cashMovementService, LiquidityAccountService liquidityAccountService) {
        this.userService = userService;
        this.cashMovementService = cashMovementService;
        this.liquidityAccountService = liquidityAccountService;
    }

    @GetMapping
    public ResponseEntity<List<CashMovementDto>> getCashMovements(
            Authentication authentication
    ){
        User user = userService.UserFromAuthentication(authentication);
        List<CashMovement> cashMovements = cashMovementService.getCashMovementsByUserId(user);
        List<CashMovementDto> cashMovementDtos =
                cashMovements.stream()
                        .map(CashMovementDto::new)
                        .toList();
        return ResponseEntity.ok(cashMovementDtos);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CashMovementDto> createCashMovement(
            Authentication authentication,
            @Valid @RequestBody CashMovementDto cashMovementDto
    ){
        User user = userService.UserFromAuthentication(authentication);

        // Risolviamo l'account dell'utente partendo dal nome passato nel DTO
        LiquidityAccount liquidityAccount = liquidityAccountService
                .getLiquidityAccountByPortfolioIdAndName(
                        user.getPortfolio().getId(),
                        cashMovementDto.getLiquidityAccountName()
                )
                .orElseThrow(() -> new NotFoundException("Liquidity Account Not Found"));

        // TODO: quando avrai un CategoryService potrai risolvere la categoria da categoryId
        Category category = null;

        // Costruiamo esplicitamente l'entità CashMovement
        CashMovement cashMovement = new CashMovement();
        cashMovement.setDate(cashMovementDto.getDate());
        cashMovement.setNote(cashMovementDto.getNote());
        cashMovement.setCategory(category);
        cashMovement.setAmount(cashMovementDto.getAmount());
        cashMovement.setType(cashMovementDto.getType());
        cashMovement.setLiquidityAccount(liquidityAccount);

        cashMovement = cashMovementService.saveCashMovement(cashMovement);

        liquidityAccountService.updateLiquidityAccount(
                liquidityAccount,
                cashMovementDto.getAmount(),
                cashMovementDto.getType()
        );

        return ResponseEntity.ok(new CashMovementDto(cashMovement));
    }

}
