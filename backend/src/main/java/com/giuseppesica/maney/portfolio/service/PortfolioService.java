package com.giuseppesica.maney.portfolio.service;

import com.giuseppesica.maney.portfolio.model.Portfolio;
import com.giuseppesica.maney.portfolio.model.PortfolioRepository;
import com.giuseppesica.maney.user.model.User;
import com.giuseppesica.maney.user.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for portfolio operations.
 * Manages user portfolios with authentication.
 */
@Service
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    /**
     * Gets portfolio for a user by email.
     *
     * @param email the user's email
     * @return Optional containing the portfolio if found
     */
    public Optional<Portfolio> getPortfolioByUserEmail(String email) {
        logger.debug("Fetching portfolio for user: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        return user.flatMap(u -> Optional.ofNullable(u.getPortfolio()));
    }

    /**
     * Updates portfolio for a user.
     *
     * @param email the user's email
     * @param portfolioData the updated portfolio data
     * @return the updated portfolio
     */
    public Portfolio updatePortfolioForUser(String email, Portfolio portfolioData) {
        logger.debug("Updating portfolio for user: {}", email);
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        Portfolio portfolio = user.get().getPortfolio();
        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found for user: " + email);
        }

        // Update portfolio fields (customize based on your needs)
        // portfolio.setName(portfolioData.getName());
        // ... other field updates

        Portfolio updated = portfolioRepository.save(portfolio);
        logger.info("Portfolio updated for user: {}", email);
        return updated;
    }

    /**
     * Deletes portfolio for a user.
     *
     * @param email the user's email
     */
    public void deletePortfolioForUser(String email) {
        logger.debug("Deleting portfolio for user: {}", email);
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        Portfolio portfolio = user.get().getPortfolio();
        if (portfolio != null) {
            portfolioRepository.delete(portfolio);
            logger.info("Portfolio deleted for user: {}", email);
        }
    }

    public Optional<Portfolio> findById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId);
    }
}
