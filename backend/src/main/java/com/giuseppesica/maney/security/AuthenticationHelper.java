package com.giuseppesica.maney.security;

import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Helper class for handling authentication-related operations.
 * Centralizes authentication logic to avoid code duplication across controllers.
 */
@Component
public class AuthenticationHelper {

    private final UserService userService;

    public AuthenticationHelper(UserService userService) {
        this.userService = userService;
    }

    /**
     * Extracts the authenticated user from the Spring Security Authentication object.
     *
     * @param authentication Spring Security authentication object
     * @return authenticated User entity
     * @throws UnauthorizedException if authentication is null or user is not authenticated
     * @throws NotFoundException if user is not found in database
     */
    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        try {
            return userService.UserFromAuthentication(authentication);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("User not found: " + e.getMessage());
        }
    }

    /**
     * Retrieves the portfolio of the authenticated user.
     *
     * @param authentication Spring Security authentication object
     * @return Portfolio of the authenticated user
     * @throws UnauthorizedException if user is not authenticated
     * @throws NotFoundException if user or portfolio is not found
     */
    public Portfolio getAuthenticatedUserPortfolio(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        if (user.getPortfolio() == null) {
            throw new NotFoundException("Portfolio not found for user: " + user.getEmail());
        }

        return user.getPortfolio();
    }

    /**
     * Gets the portfolio ID of the authenticated user.
     *
     * @param authentication Spring Security authentication object
     * @return portfolio ID
     * @throws UnauthorizedException if user is not authenticated
     * @throws NotFoundException if user or portfolio is not found
     */
    public Long getAuthenticatedUserPortfolioId(Authentication authentication) {
        return getAuthenticatedUserPortfolio(authentication).getId();
    }

    /**
     * Validates that the given portfolio ID belongs to the authenticated user.
     *
     * @param authentication Spring Security authentication object
     * @param portfolioId portfolio ID to validate
     * @throws UnauthorizedException if user is not authenticated
     * @throws NotFoundException if user or portfolio is not found
     * @throws ForbiddenException if the portfolio does not belong to the user
     */
    public void validatePortfolioAccess(Authentication authentication, Long portfolioId) {
        Long userPortfolioId = getAuthenticatedUserPortfolioId(authentication);

        if (!userPortfolioId.equals(portfolioId)) {
            throw new ForbiddenException("Access denied to portfolio: " + portfolioId);
        }
    }

    /**
     * Validates that the authenticated user has access to the specified resource
     * by checking if it belongs to their portfolio.
     *
     * @param authentication Spring Security authentication object
     * @param resourcePortfolioId portfolio ID associated with the resource
     * @param resourceType type of resource (e.g., "LiquidityAccount", "IlliquidAsset")
     * @throws UnauthorizedException if user is not authenticated
     * @throws NotFoundException if user or portfolio is not found
     * @throws ForbiddenException if the resource does not belong to the user's portfolio
     */
    public void validateResourceAccess(Authentication authentication, Long resourcePortfolioId, String resourceType) {
        Long userPortfolioId = getAuthenticatedUserPortfolioId(authentication);

        if (!userPortfolioId.equals(resourcePortfolioId)) {
            throw new ForbiddenException("Access denied to " + resourceType + ": not in user's portfolio");
        }
    }
}

