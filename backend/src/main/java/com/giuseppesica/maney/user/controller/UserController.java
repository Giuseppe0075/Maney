package com.giuseppesica.maney.user.controller;

import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.dto.PortfolioDto;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.service.PortfolioService;
import com.giuseppesica.maney.user.dto.UserRegistrationDto;
import com.giuseppesica.maney.user.dto.UserLoginDto;
import com.giuseppesica.maney.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.dto.UserResponseDto;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final PortfolioService portfolioService;
    private final IlliquidAssetService illiquidAssetService;

    @Autowired
    public UserController(UserService userService, PortfolioService portfolioService, IlliquidAssetService illiquidAssetService) {
        this.userService = userService;
        this.portfolioService = portfolioService;
        this.illiquidAssetService = illiquidAssetService;
    }


    /**
     * Authenticates a user and initiates a session.
     * Spring Security handles session creation after successful authentication.
     *
     * @param loginDto the login credentials (email and password)
     * @return authenticated user information
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody UserLoginDto loginDto,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        logger.info("POST /user/login - email={}", loginDto.getEmail());
        User user = userService.authenticate(loginDto.getEmail(), loginDto.getPassword());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        request.getSession(true);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        logger.info("Login OK - session id={}", request.getSession(false).getId());
        return ResponseEntity.ok(new UserResponseDto(user));
    }


    /**
     * Registers a new user account.
     * Creates a new user and associated portfolio.
     *
     * @param registrationDto the registration data
     * @return created user information
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        logger.info("POST /user/register - new user registration with email: {}", registrationDto.getEmail());
        User user = userService.register(
                registrationDto.getUsername(),
                registrationDto.getEmail(),
                registrationDto.getPassword()
        );
        logger.info("User registered successfully with ID: {}", user.getId());
        Portfolio portfolio = new Portfolio();
        user.setPortfolio(portfolio);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDto(user));
    }

    /**
     * Retrieves the portfolio of the authenticated user.
     *
     * @param authentication the authentication object
     * @return the user's portfolio
     */
    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioDto> getPortfolio(Authentication authentication) {
        // Check for authentication
        User user;
        try{
            user = userService.UserFromAuthentication(authentication);
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Ensure the user has a portfolio
        if (user.getPortfolio() == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found for user: " + authentication.getName());
        }

        // Retrieve portfolio
        Long portfolioId = user.getPortfolio().getId();
        Portfolio portfolio = portfolioService.findById(portfolioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Retrieve the assets
        List<IlliquidAssetDto> illiquidAssetDtos = illiquidAssetService.getIlliquidAssets(portfolioId);


        // Create and return PortfolioDto
        PortfolioDto portfolioDto = new PortfolioDto(portfolio);
        portfolioDto.setIlliquidAssets(illiquidAssetDtos);

        return ResponseEntity.ok(portfolioDto);

    }
}
