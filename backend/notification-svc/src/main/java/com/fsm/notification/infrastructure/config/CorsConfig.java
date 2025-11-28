package com.fsm.notification.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the notification service.
 * Allows frontend applications running on different ports to access the API.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",  // Shell
                        "http://localhost:5174",  // Identity Frontend
                        "http://localhost:5175",  // Location Services
                        "http://localhost:5176",  // Task Management
                        "http://localhost:5178"   // Technician Mobile
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
