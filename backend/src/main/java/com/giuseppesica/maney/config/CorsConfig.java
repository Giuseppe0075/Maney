package com.giuseppesica.maney.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configurazione CORS per consentire le richieste dal frontend.
 * Configura origin, metodi HTTP, header e credenziali.
 */
@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origini consentite (aggiungere solo quelle necessarie in produzione)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5176",
                "http://localhost:8080"
        ));

        // Metodi HTTP consentiti
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Header consentiti
        config.setAllowedHeaders(List.of("*"));

        // Consenti credenziali (cookie, XSRF token, ecc.)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("[CORS] Config: origins={}, methods={}, credentials={}",
                config.getAllowedOrigins(), config.getAllowedMethods(), config.getAllowCredentials());

        return source;
    }
}

