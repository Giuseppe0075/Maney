package com.giuseppesica.maney.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler per loggare gli accessi negati (403), in particolare gli errori CSRF.
 */
@Component
public class SecurityEventsLogger implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventsLogger.class);
    private final AccessDeniedHandler delegate = new AccessDeniedHandlerImpl();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String origin = request.getHeader("Origin");
        String xsrfHeader = request.getHeader("X-XSRF-TOKEN");

        // Distingue tra errore CSRF e altri accessi negati
        if (accessDeniedException instanceof CsrfException) {
            log.error("[SECURITY] CSRF denied: {} {} origin={} csrfHeaderPresent={} msg={}",
                    request.getMethod(), request.getRequestURI(), origin, xsrfHeader != null,
                    accessDeniedException.getMessage());
        } else {
            log.error("[SECURITY] Access denied: {} {} origin={} msg={}",
                    request.getMethod(), request.getRequestURI(), origin, accessDeniedException.getMessage());
        }

        delegate.handle(request, response, accessDeniedException);
    }
}

