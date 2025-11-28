package com.fsm.location.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the Location Service.
 * Configures Spring Security with JWT-based authentication.
 * 
 * Note: In production, this service would validate JWT tokens from the identity-svc.
 * For now, it provides basic security setup that can be extended.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    @SuppressWarnings("java:S4502") // CSRF disabled intentionally for stateless JWT API
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with configured sources
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF protection is disabled because this is a stateless REST API using JWT tokens.
                // JWT tokens are not stored in cookies and must be explicitly included in each request,
                // which inherently protects against CSRF attacks.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow Swagger/OpenAPI endpoints
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Allow H2 console for development
                        .requestMatchers("/h2-console/**").permitAll()
                        // Allow all API requests (TODO: Add JWT validation in production)
                        .requestMatchers("/api/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                // Allow H2 console frames
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        
        return http.build();
    }
}
