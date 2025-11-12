// java
package com.giuseppesica.maney.config;

import com.giuseppesica.maney.config.security.PortfolioOwnerAuthorizationManager;
import com.giuseppesica.maney.config.security.RequestResponseLoggingFilter;
import com.giuseppesica.maney.config.security.SecurityEventsLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter() {
        return new RequestResponseLoggingFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   PortfolioOwnerAuthorizationManager portfolioOwnerAuthorizationManager,
                                                   RequestResponseLoggingFilter requestResponseLoggingFilter,
                                                   SecurityEventsLogger securityEventsLogger) throws Exception {

        http
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/register",
                                "/users/register",
                                "/api/users/register",
                                "/api/users/login",
                                "/users/login"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/favicon.ico",
                                "/css/**", "/js/**", "/assets/**", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/register", "/users/register", "/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/register", "/users/register", "/users/login").permitAll()
                        .requestMatchers("/users/login", "/login", "/csrf").permitAll()
                        .requestMatchers("/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/portfolio/{id}")
                        .access(portfolioOwnerAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, "/portfolio/{id}")
                        .access(portfolioOwnerAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, "/portfolio/{id}")
                        .access(portfolioOwnerAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, "/portfolio").authenticated()
                        .requestMatchers(HttpMethod.GET, "/portfolio").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.loginPage("/login").permitAll())
                .logout(Customizer.withDefaults())
                .exceptionHandling(ex -> ex.accessDeniedHandler(securityEventsLogger))
                .addFilterBefore(requestResponseLoggingFilter, CsrfFilter.class);

        return http.build();
    }
}
