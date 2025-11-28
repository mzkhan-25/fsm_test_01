package com.fsm.identity.infrastructure.config;

import com.fsm.identity.infrastructure.security.JwtAuthenticationFilter;
import com.fsm.identity.infrastructure.security.RoleAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the identity service.
 * Configures JWT-based authentication and authorization with role-based access control.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RoleAuthorizationFilter roleAuthorizationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    
    /**
     * Password encoder bean using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Security filter chain configuration
     * Disables session management and uses JWT for authentication.
     * Adds JWT authentication and role authorization filters to the security chain.
     * 
     * CSRF protection is disabled because this is a stateless REST API using JWT tokens.
     * CSRF attacks only work when cookies are used for authentication. Since this API
     * uses JWT tokens in Authorization headers (not cookies), CSRF protection is not needed.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with configured sources
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF disabled for stateless JWT authentication (tokens in headers, not cookies)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll() // Allow inter-service communication
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Add role authorization filter after JWT authentication filter
                .addFilterAfter(roleAuthorizationFilter, JwtAuthenticationFilter.class);
        
        return http.build();
    }
}
