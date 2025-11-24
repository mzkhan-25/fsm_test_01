package com.fsm.identity.infrastructure.security;

import com.fsm.identity.domain.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private FilterChain filterChain;

    private JwtUtil jwtUtil;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        ReflectionTestUtils.setField(jwtUtil, "webExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "mobileExpiration", 2592000000L);
        
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
    }

    @Test
    void testDoFilterInternalWithValidToken() throws ServletException, IOException {
        // Generate valid token
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Execute filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify authentication was set
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("test@example.com", auth.getPrincipal());
        assertTrue(auth.isAuthenticated());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        
        // Verify filter chain continued
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithoutAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify authentication was not set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        
        // Verify filter chain continued
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithNonBearerToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify authentication was not set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        
        // Verify filter chain continued
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithInvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.here");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify authentication was not set (invalid token should be ignored)
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        
        // Verify filter chain continued
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithDifferentRoles() throws ServletException, IOException {
        Role[] roles = {Role.ADMIN, Role.DISPATCHER, Role.SUPERVISOR, Role.TECHNICIAN};
        
        for (Role role : roles) {
            SecurityContextHolder.clearContext();
            
            String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", role, false);
            
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            
            MockHttpServletResponse response = new MockHttpServletResponse();
            
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name())));
        }
    }

    @Test
    void testDoFilterInternalDoesNotOverrideExistingAuthentication() throws ServletException, IOException {
        // Set up existing authentication
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Verify existing authentication was not replaced
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        
        verify(filterChain).doFilter(request, response);
    }
}
