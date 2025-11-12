package com.giuseppesica.maney.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filtro per loggare tutte le richieste e risposte HTTP.
 * Logga metodo, URI, origin CORS, header XSRF, utente autenticato e status della risposta.
 */
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        String acrm = request.getHeader("Access-Control-Request-Method");
        String acrh = request.getHeader("Access-Control-Request-Headers");
        String xsrfHeader = request.getHeader("X-XSRF-TOKEN");

        // Estrae il cookie XSRF se presente
        String xsrfCookie = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
                .filter(c -> "XSRF-TOKEN".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);

        // Recupera l'utente autenticato dal contesto di sicurezza
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null && auth.isAuthenticated()) ? String.valueOf(auth.getPrincipal()) : "anonymous";

        log.info("[REQ] {} {} origin={} referer={} ACRM={} ACRH={} user={} csrfHeaderPresent={} csrfCookiePresent={}",
                method, uri, origin, referer, acrm, acrh, user,
                xsrfHeader != null, xsrfCookie != null);

        try {
            chain.doFilter(request, response);
        } finally {
            String allowOrigin = response.getHeader("Access-Control-Allow-Origin");
            String allowCred = response.getHeader("Access-Control-Allow-Credentials");
            log.info("[RES] {} {} status={} ACAO={} ACAC={}",
                    method, uri, response.getStatus(), allowOrigin, allowCred);
        }
    }
}

