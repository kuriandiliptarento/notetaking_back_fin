// src/main/java/com/tarento/notesapp/config/CorsConfig.java
package com.tarento.notesapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Allow credentials (cookies / Authorization header)
        cfg.setAllowCredentials(true);

        // Only allow your frontend origins (do NOT use "*")
        // Adjust ports to match your dev environment
        cfg.setAllowedOrigins(List.of(
            "http://localhost:5173",  // Vite default
            "http://localhost:3000"   // CRA default (if you use it)
        ));
        // If you want pattern matching (e.g. any localhost port) use:
        // cfg.setAllowedOriginPatterns(List.of("http://localhost:*"));

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        // Expose Authorization so frontend can read it if backend sets it
        cfg.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
