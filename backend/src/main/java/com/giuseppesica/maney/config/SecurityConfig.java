package com.giuseppesica.maney.config;

import com.giuseppesica.maney.config.security.PortfolioOwnerAuthorizationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the application.
 * Defines authentication, authorization, CORS, and CSRF protection rules.
 */
@Configuration
public class SecurityConfig {

    /**
     * Defines the password encoder bean.
     * Uses BCrypt algorithm for secure password hashing.
     *
     * @return PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain.
     * Handles CORS, CSRF protection, authentication, and authorization rules.
     *
     * @param http HttpSecurity builder
     * @param corsConfigurationSource CORS configuration source
     * @param portfolioOwnerAuthorizationManager custom authorization manager for portfolio ownership
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   PortfolioOwnerAuthorizationManager portfolioOwnerAuthorizationManager) throws Exception {

        http
                // Enable CORS with the provided configuration source
                .cors(c -> c.configurationSource(corsConfigurationSource))

                // Configure CSRF protection using cookie-based tokens
                // withHttpOnlyFalse() allows JavaScript to access the token
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )

                // Add filter after CSRF filter to ensure XSRF-TOKEN cookie is set on first GET request
                .addFilterAfter((request, response, chain) -> chain.doFilter(request, response), CsrfFilter.class)

                // Configure URL-based authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public static resources - no authentication required
                        .requestMatchers("/", "/index.html", "/favicon.ico",
                                "/css/**", "/js/**", "/assets/**", "/webjars/**").permitAll()

                        // Public API endpoints for registration
                        .requestMatchers(HttpMethod.POST, "/api/register", "/users/register").permitAll()

                        // Public login endpoints
                        .requestMatchers("/users/login", "/login", "/csrf").permitAll()

                        // User profile endpoints - requires authentication only (no ID parameter = no IDOR risk)
                        .requestMatchers("/me/**").authenticated()

                        // Portfolio endpoints with ID - uses custom authorization manager to verify ownership
                        .requestMatchers(HttpMethod.GET,    "/portfolio/{id}")
                        .access(portfolioOwnerAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT,    "/portfolio/{id}")
                        .access(portfolioOwnerAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, "/portfolio/{id}")
                        .access(portfolioOwnerAuthorizationManager)

                        // Portfolio creation and listing - requires authentication
                        .requestMatchers(HttpMethod.POST, "/portfolio").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/portfolio").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Enable form-based login
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )

                // Enable logout with default settings
                .logout(Customizer.withDefaults());

        return http.build();
    }
}
