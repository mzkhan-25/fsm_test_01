package com.fsm.identity.infrastructure.security;

import com.fsm.identity.domain.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authorization filter that checks if authenticated user has required role.
 * Uses @RequireRole annotation on controller methods to determine required roles.
 */
@Component
@Slf4j
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping handlerMapping;

    public RoleAuthorizationFilter(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Get handler method for this request
            HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);
            
            if (handlerChain != null && handlerChain.getHandler() instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();
                
                // Check for @RequireRole annotation on method first, then class
                RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
                if (requireRole == null) {
                    requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
                }
                
                // If @RequireRole is present, check authorization
                if (requireRole != null) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    
                    // If not authenticated, return 401
                    if (authentication == null || !authentication.isAuthenticated()) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        return;
                    }
                    
                    // Extract user's roles
                    Set<String> userRoles = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .map(auth -> auth.replace("ROLE_", ""))
                            .collect(Collectors.toSet());
                    
                    // Check if user has ADMIN role (ADMIN has access to everything)
                    if (userRoles.contains(Role.ADMIN.name())) {
                        log.debug("ADMIN user has access to all endpoints");
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // Check if user has any of the required roles
                    Set<String> requiredRoles = Arrays.stream(requireRole.value())
                            .map(Role::name)
                            .collect(Collectors.toSet());
                    
                    boolean hasRequiredRole = userRoles.stream()
                            .anyMatch(requiredRoles::contains);
                    
                    if (!hasRequiredRole) {
                        log.warn("User with roles {} attempted to access endpoint requiring roles {}",
                                userRoles, requiredRoles);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        return;
                    }
                    
                    log.debug("User has required role. Granting access.");
                }
            }
        } catch (Exception e) {
            log.error("Error in role authorization: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
