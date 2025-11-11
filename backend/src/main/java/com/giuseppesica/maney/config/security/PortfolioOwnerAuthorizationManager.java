package com.giuseppesica.maney.config.security;

import com.giuseppesica.maney.portfolio.dto.Portfolio;
import com.giuseppesica.maney.portfolio.dto.PortfolioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class PortfolioOwnerAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        HttpServletRequest request = context.getRequest();
        Long portfolioId = extractIdFromUri(request.getRequestURI(), "/portfolio/");
        if (portfolioId == null) {
            return new AuthorizationDecision(false);
        }

        Optional<Portfolio> p = portfolioRepository.findById(portfolioId);
        if (p.isEmpty() || p.get().getUser() == null) {
            return new AuthorizationDecision(false);
        }

        String ownerUsername = p.get().getUser().getUsername();
        boolean allowed = ownerUsername != null && ownerUsername.equals(auth.getName());
        return new AuthorizationDecision(allowed);
    }

    private Long extractIdFromUri(String uri, String prefix) {
        int idx = uri.indexOf(prefix);
        if (idx == -1) return null;
        String tail = uri.substring(idx + prefix.length());
        // consider cases like "/portfolio/123", "/portfolio/123/..." -> take first segment
        String[] parts = tail.split("/");
        if (parts.length == 0 || parts[0].isBlank()) return null;
        try {
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
