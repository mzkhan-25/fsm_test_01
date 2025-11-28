package com.fsm.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for the identity service.
 * Allows frontend applications running on different ports to access the API.
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow frontend origins (all dev ports)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Shell
                "http://localhost:5174",  // Identity Frontend
                "http://localhost:5175",  // Location Services
                "http://localhost:5176",  // Task Management
                "http://localhost:5178"   // Technician Mobile
        ));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Max age for preflight cache
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
