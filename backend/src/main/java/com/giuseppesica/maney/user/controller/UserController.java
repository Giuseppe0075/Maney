package com.giuseppesica.maney.user.controller;

import com.giuseppesica.maney.account.dto.LiquidityAccountDto;
import com.giuseppesica.maney.account.service.LiquidityAccountService;
import com.giuseppesica.maney.category.model.CategoryDto;
import com.giuseppesica.maney.category.service.CategoryService;
import com.giuseppesica.maney.illiquidasset.dto.IlliquidAssetDto;
import com.giuseppesica.maney.illiquidasset.service.IlliquidAssetService;
import com.giuseppesica.maney.portfolio.dto.PortfolioDto;
import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.service.PortfolioService;
import com.giuseppesica.maney.security.AuthenticationHelper;
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

import java.util.List;

/**
 * REST controller for user management.
 * Handles user authentication, registration, and user-related operations.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final PortfolioService portfolioService;
    private final IlliquidAssetService illiquidAssetService;
    private final LiquidityAccountService liquidityAccountService;
    private final AuthenticationHelper authHelper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final CategoryService categoryService;

    /**
     * Constructor for dependency injection.
     *
     * @param userService Service for user operations
     * @param portfolioService Service for portfolio operations
     * @param illiquidAssetService Service for illiquid asset operations
     * @param liquidityAccountService Service for liquidity account operations
     * @param authHelper Helper for authentication operations
     */
    @Autowired
    public UserController(
            UserService userService,
            PortfolioService portfolioService,
            IlliquidAssetService illiquidAssetService,
            LiquidityAccountService liquidityAccountService,
            AuthenticationHelper authHelper,
            CategoryService categoryService) {
        this.userService = userService;
        this.portfolioService = portfolioService;
        this.illiquidAssetService = illiquidAssetService;
        this.liquidityAccountService = liquidityAccountService;
        this.authHelper = authHelper;
        this.categoryService = categoryService;
    }


    /**
     * Authenticates a user and initiates a session.
     * Spring Security handles session creation after successful authentication.
     *
     * @param loginDto Login credentials (email and password)
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @return ResponseEntity with authenticated user information
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody UserLoginDto loginDto,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        logger.info("POST /user/login - email={}", loginDto.getEmail());
        User user = userService.authenticate(loginDto.getEmail(), loginDto.getPassword());

        // Create authentication token
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Set security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Save context in session
        request.getSession(true);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        logger.info("Login OK - session id={}", request.getSession(false).getId());
        return ResponseEntity.ok(new UserResponseDto(user));
    }


    /**
     * Registers a new user account.
     * Creates a new user and associated portfolio.
     *
     * @param registrationDto Registration data (username, email, password)
     * @return ResponseEntity with created user information and status 201
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

        // Create portfolio for new user
        Portfolio portfolio = new Portfolio();
        user.setPortfolio(portfolio);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDto(user));
    }

    /**
     * Retrieves the portfolio of the authenticated user.
     * Includes all illiquid assets and liquidity accounts in the portfolio.
     *
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with PortfolioDto containing portfolio data and assets
     */
    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioDto> getPortfolio(Authentication authentication) {
        // Get authenticated user's portfolio using helper
        Portfolio portfolio = authHelper.getAuthenticatedUserPortfolio(authentication);
        Long portfolioId = portfolio.getId();

        // Retrieve all assets
        List<IlliquidAssetDto> illiquidAssetDtos = illiquidAssetService.getIlliquidAssets(portfolioId);
        List<LiquidityAccountDto> liquidityAccountDtos = liquidityAccountService.getLiquidityAccounts(portfolioId);

        // Create and return PortfolioDto
        PortfolioDto portfolioDto = new PortfolioDto(portfolio);
        portfolioDto.setIlliquidAssets(illiquidAssetDtos);
        portfolioDto.setLiquidityAccounts(liquidityAccountDtos);

        return ResponseEntity.ok(portfolioDto);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getUserCategories(Authentication authentication) {
        User user = userService.UserFromAuthentication(authentication);
        List<CategoryDto> categories = categoryService.findByUserId(user.getId())
                .stream()
                .map(CategoryDto::new)
                .toList();
        return ResponseEntity.ok(categories);
    }
}
