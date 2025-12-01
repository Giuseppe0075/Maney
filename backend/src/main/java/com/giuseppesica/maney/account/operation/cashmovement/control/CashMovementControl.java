package com.giuseppesica.maney.account.operation.cashmovement.control;

import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.operation.cashmovement.model.CashMovement;
import com.giuseppesica.maney.account.operation.cashmovement.model.CashMovementDto;
import com.giuseppesica.maney.account.operation.cashmovement.service.CashMovementService;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.category.model.Category;
import com.giuseppesica.maney.category.service.CategoryService;
import com.giuseppesica.maney.security.NotFoundException;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import com.giuseppesica.maney.utils.CashMovementType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/portfolio/liquidity-accounts/cash-movements")
public class CashMovementControl {

    private final UserService userService;
    private final CashMovementService cashMovementService;
    private final LiquidityAccountService liquidityAccountService;
    private final CategoryService categoryService;

    @Autowired
    public CashMovementControl(UserService userService, CashMovementService cashMovementService, LiquidityAccountService liquidityAccountService, CategoryService categoryService) {
        this.userService = userService;
        this.cashMovementService = cashMovementService;
        this.liquidityAccountService = liquidityAccountService;
        this.categoryService = categoryService;
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

    @GetMapping("/{id}")
    public ResponseEntity<CashMovementDto> getCashMovementById(
            Authentication authentication,
            @PathVariable Long id
            ){
        User user = userService.UserFromAuthentication(authentication);
        CashMovement cashMovement = cashMovementService.getCashMovementByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Cash Movement Not Found"));
        return ResponseEntity.ok(new CashMovementDto(cashMovement));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CashMovementDto> createCashMovement(
            Authentication authentication,
            @Valid @RequestBody CashMovementDto cashMovementDto
    ){
        User user = userService.UserFromAuthentication(authentication);
        
        LiquidityAccount liquidityAccount = liquidityAccountService
                .getLiquidityAccountByPortfolioIdAndName(
                        user.getPortfolio().getId(),
                        cashMovementDto.getLiquidityAccountName()
                )
                .orElseThrow(() -> new NotFoundException("Liquidity Account Not Found"));

        Category category = categoryService.findByUserAndId(
                user.getId(),
                cashMovementDto.getCategoryId()
        ).orElseThrow(() -> new NotFoundException("Category Not Found"));

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

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<CashMovementDto> updateCashMovement(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CashMovementDto cashMovementDto
    ){
        User user = userService.UserFromAuthentication(authentication);
        CashMovement cashMovementToUpdate = cashMovementService.getCashMovementByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Cash Movement Not Found"));
        // Revert previous cash movement effect
        liquidityAccountService.updateLiquidityAccount(
                cashMovementToUpdate.getLiquidityAccount(),
                cashMovementToUpdate.getAmount(),
                cashMovementToUpdate.getType() == CashMovementType.INCOME ? CashMovementType.OUTCOME : CashMovementType.INCOME
        );
        // Apply new cash movement effect
        liquidityAccountService.updateLiquidityAccount(
                cashMovementToUpdate.getLiquidityAccount(),
                cashMovementDto.getAmount(),
                cashMovementDto.getType()
        );

        // Save cash movement changes
        cashMovementToUpdate.setDate(cashMovementDto.getDate());
        cashMovementToUpdate.setNote(cashMovementDto.getNote());
        cashMovementToUpdate.setAmount(cashMovementDto.getAmount());
        cashMovementToUpdate.setType(cashMovementDto.getType());
        CashMovement updatedCm = cashMovementService.saveCashMovement(cashMovementToUpdate);
        return ResponseEntity.ok(new CashMovementDto(updatedCm));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCashMovement(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.UserFromAuthentication(authentication);
        CashMovement cashMovementToDelete = cashMovementService.getCashMovementByIdAndUserId(id, user)
                .orElseThrow(() -> new NotFoundException("Cash Movement Not Found"));
        // Revert cash movement effect
        liquidityAccountService.updateLiquidityAccount(
                cashMovementToDelete.getLiquidityAccount(),
                cashMovementToDelete.getAmount(),
                cashMovementToDelete.getType() == CashMovementType.INCOME ? CashMovementType.OUTCOME : CashMovementType.INCOME
        );
        cashMovementService.deleteCashMovement(cashMovementToDelete);
        return ResponseEntity.noContent().build();
    }

}
