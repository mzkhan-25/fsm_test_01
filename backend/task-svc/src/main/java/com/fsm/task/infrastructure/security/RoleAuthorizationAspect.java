package com.fsm.task.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aspect for role-based authorization using @RequireRole annotation.
 * Checks if the authenticated user has the required role to access the method.
 */
@Aspect
@Component
@Slf4j
public class RoleAuthorizationAspect {
    
    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If not authenticated, throw access denied
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Unauthenticated access attempt to protected endpoint");
            sendUnauthorizedResponse();
            return null;
        }
        
        // Extract user's roles
        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .collect(Collectors.toSet());
        
        // Check if user has ADMIN role (ADMIN has access to everything)
        if (userRoles.contains(Role.ADMIN.name())) {
            log.debug("ADMIN user has access to all endpoints");
            return joinPoint.proceed();
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
            sendForbiddenResponse();
            return null;
        }
        
        log.debug("User has required role. Granting access.");
        return joinPoint.proceed();
    }
    
    private void sendUnauthorizedResponse() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletResponse response = attrs.getResponse();
                if (response != null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                }
            }
        } catch (Exception e) {
            log.error("Error sending unauthorized response", e);
        }
    }
    
    private void sendForbiddenResponse() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletResponse response = attrs.getResponse();
                if (response != null) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                }
            }
        } catch (Exception e) {
            log.error("Error sending forbidden response", e);
        }
    }
}
