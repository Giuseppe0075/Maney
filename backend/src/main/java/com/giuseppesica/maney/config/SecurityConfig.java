package com.giuseppesica.maney.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security configuration for REST API.
 * Minimal and clean filterchain for authentication and authorization.
 *
 * URL Structure:
 * - Public layer: /login, /register, /homepage
 * - Authenticated layer: /user/* (user/portfolio, user/portfolio/asset, etc.)
 */
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {

        log.info("[SECURITY] Initializing SecurityFilterChain");

        http
            // Enable CORS from configured origins
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // CSRF protection with token repository
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Ignore CSRF for public endpoints (registration and login)
                .ignoringRequestMatchers(
                    "POST /api/login",
                    "POST /api/register"
                )
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public layer - accessible without authentication
                .requestMatchers("GET /api/csrf").permitAll()
                .requestMatchers("POST /api/login").permitAll()
                .requestMatchers("POST /api/register").permitAll()
                .requestMatchers("GET /api/homepage").permitAll()
                .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                .requestMatchers("/public/**").permitAll()

                // Authenticated layer - requires authentication
                .requestMatchers("/api/user/**").authenticated()

                // Default: deny all other requests
                .anyRequest().authenticated()
            )

            // Disable form login (this is a REST API, not a web app)
            .formLogin(AbstractHttpConfigurer::disable)

            // Use HTTP Basic authentication
            .httpBasic(basic -> basic.realmName("Maney API"));

        log.info("[SECURITY] SecurityFilterChain initialized successfully");

        return http.build();
    }
}
