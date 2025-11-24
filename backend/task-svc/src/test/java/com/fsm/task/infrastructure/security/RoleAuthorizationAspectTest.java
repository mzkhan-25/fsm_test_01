package com.fsm.task.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleAuthorizationAspect
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleAuthorizationAspectTest {
    
    private RoleAuthorizationAspect aspect;
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    @Mock
    private RequireRole requireRole;
    
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    @BeforeEach
    void setUp() {
        aspect = new RoleAuthorizationAspect();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void testAdminHasAccessToAllEndpoints() throws Throwable {
        // Set up ADMIN role
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "admin@fsm.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(requireRole.value()).thenReturn(new Role[]{Role.DISPATCHER});
        when(joinPoint.proceed()).thenReturn("success");
        
        Object result = aspect.checkRole(joinPoint, requireRole);
        
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }
    
    @Test
    void testDispatcherHasAccessToDispatcherEndpoint() throws Throwable {
        // Set up DISPATCHER role
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "dispatcher@fsm.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_DISPATCHER")));
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(requireRole.value()).thenReturn(new Role[]{Role.DISPATCHER});
        when(joinPoint.proceed()).thenReturn("success");
        
        Object result = aspect.checkRole(joinPoint, requireRole);
        
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }
    
    @Test
    void testTechnicianDeniedAccessToDispatcherEndpoint() throws Throwable {
        // Set up TECHNICIAN role
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "tech@fsm.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_TECHNICIAN")));
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(requireRole.value()).thenReturn(new Role[]{Role.DISPATCHER, Role.ADMIN});
        
        Object result = aspect.checkRole(joinPoint, requireRole);
        
        assertNull(result);
        verify(joinPoint, never()).proceed();
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }
    
    @Test
    void testUnauthenticatedUserDeniedAccess() throws Throwable {
        // No authentication set
        when(requireRole.value()).thenReturn(new Role[]{Role.DISPATCHER});
        
        Object result = aspect.checkRole(joinPoint, requireRole);
        
        assertNull(result);
        verify(joinPoint, never()).proceed();
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    
    @Test
    void testAnonymousUserDeniedAccess() throws Throwable {
        // Set up anonymous user
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "anonymousUser", null, List.of());
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(requireRole.value()).thenReturn(new Role[]{Role.DISPATCHER});
        
        Object result = aspect.checkRole(joinPoint, requireRole);
        
        assertNull(result);
        verify(joinPoint, never()).proceed();
    }
    
    @Test
    void testMultipleAllowedRoles() throws Throwable {
        // Set up SUPERVISOR role with endpoint allowing ADMIN, DISPATCHER, SUPERVISOR
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "supervisor@fsm.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERVISOR")));
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(requireRole.value()).thenReturn(new Role[]{Role.ADMIN, Role.DISPATCHER, Role.SUPERVISOR});
        when(joinPoint.proceed()).thenReturn("success");
        
        Object result = aspect.checkRole(joinPoint, requireRole);
        
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }
    
    @Test
    void testRoleWithoutRolePrefix() throws Throwable {
        // Some systems might not add ROLE_ prefix
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "dispatcher@fsm.com", null,
                List.of(new SimpleGrantedAuthority("DISPATCHER")));
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(requireRole.value()).thenReturn(new Role[]{Role.DISPATCHER});
        when(joinPoint.proceed()).thenReturn("success");
        
        Object result = aspect.checkRole(joinPoint, requireRole);
        
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }
    
    @Test
    void testNotAuthenticatedReturnsForbidden() throws Throwable {
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "user@fsm.com", null, List.of());
        auth.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(requireRole.value()).thenReturn(new Role[]{Role.DISPATCHER});
        
        Object result = aspect.checkRole(joinPoint, requireRole);
        
        assertNull(result);
        verify(joinPoint, never()).proceed();
    }
    
    @Test
    void testNoRequestContextHandledGracefully() throws Throwable {
        RequestContextHolder.resetRequestAttributes();
        
        // Set up unauthenticated user
        when(requireRole.value()).thenReturn(new Role[]{Role.DISPATCHER});
        
        // Should not throw exception
        Object result = aspect.checkRole(joinPoint, requireRole);
        assertNull(result);
    }
}
