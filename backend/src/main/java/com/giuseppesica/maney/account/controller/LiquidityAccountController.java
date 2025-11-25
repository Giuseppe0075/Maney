package com.giuseppesica.maney.account.controller;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.model.LiquidityAccount;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user/liquidity-account")
public class LiquidityAccountController {
    private final UserService userService;
    private final LiquidityAccountService liquidityAccountService;

    @Autowired
    public LiquidityAccountController(LiquidityAccountService liquidityAccountService, UserService userService) {
        this.liquidityAccountService = liquidityAccountService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<LiquidityAccountDto> createLiquidityAccount(
            Authentication authentication,
            @Valid @RequestBody LiquidityAccountDto liquidityAccountDto
    ){
        User user;
        try{
            user = userService.UserFromAuthentication(authentication);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Portfolio portfolio = user.getPortfolio();
        if (liquidityAccountDto.getPortfolioId() != portfolio.getId()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            LiquidityAccount liquidityAccount = liquidityAccountService.saveLiquidityAccount(liquidityAccountDto);
            LiquidityAccountDto responseDto = new LiquidityAccountDto(liquidityAccount);
            return ResponseEntity.ok(responseDto);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
