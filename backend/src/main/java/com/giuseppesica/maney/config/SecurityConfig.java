package com.giuseppesica.maney.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 *
 * URL Structure:
 * - /user/login → Pubblico
 * - /user/register → Pubblico
 * - /api/** → Pubblico (utilities come /api/csrf)
 * - /user/** → Privato (richiede autenticazione)
 *
 * Authorization:
 * - Accesso senza autenticazione a endpoint privati → Redirect a /login
 * - Accesso a risorse di altri utenti → 404 Not Found
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/user/login", "/user/register")
                )

                .authorizeHttpRequests(auth -> auth
                        // Pubblici: login, register e tutto sotto /api
                        .requestMatchers("/user/login", "/user/register").permitAll()
                        .requestMatchers("/api/**").permitAll()

                        // Privati: tutto il resto sotto /user
                        .requestMatchers("/user/**").authenticated()

                        // Risorse statiche
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/public/**").permitAll()

                        // Default: richiede autenticazione
                        .anyRequest().authenticated()
                )

                // Redirect a /login se non autenticato
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("[SECURITY] Unauthorized access to {} - Redirecting to /login", request.getRequestURI());
                            response.sendRedirect("/login");
                        })
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(basic -> basic.realmName("Maney API"));

        log.info("[SECURITY] SecurityFilterChain initialized successfully");

        return http.build();
    }
}
