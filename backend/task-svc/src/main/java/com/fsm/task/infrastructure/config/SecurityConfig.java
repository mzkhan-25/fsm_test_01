package com.fsm.task.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Task Management Service.
 * Configures Spring Security with JWT-based authentication.
 * 
 * Note: In production, this service would validate JWT tokens from the identity-svc.
 * For now, it provides basic security setup that can be extended.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow Swagger/OpenAPI endpoints
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Allow H2 console for development
                        .requestMatchers("/h2-console/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                // Allow H2 console frames
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        
        return http.build();
    }
}
