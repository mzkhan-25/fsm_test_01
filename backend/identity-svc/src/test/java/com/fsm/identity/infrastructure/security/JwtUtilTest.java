package com.fsm.identity.infrastructure.security;

import com.fsm.identity.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil
 */
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set test properties
        ReflectionTestUtils.setField(jwtUtil, "secret", "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        ReflectionTestUtils.setField(jwtUtil, "webExpiration", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtUtil, "mobileExpiration", 2592000000L); // 30 days
    }
    
    @Test
    void testGenerateTokenForWeb() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testGenerateTokenForMobile() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, true);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
        String username = jwtUtil.extractUsername(token);
        
        assertEquals("test@example.com", username);
    }
    
    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
        
        assertNotNull(jwtUtil.extractExpiration(token));
        assertTrue(jwtUtil.extractExpiration(token).getTime() > System.currentTimeMillis());
    }
    
    @Test
    void testValidateTokenWithValidToken() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
        
        assertTrue(jwtUtil.validateToken(token, "test@example.com"));
    }
    
    @Test
    void testValidateTokenWithWrongUsername() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
        
        assertFalse(jwtUtil.validateToken(token, "wrong@example.com"));
    }
    
    @Test
    void testTokenContainsUserId() {
        String token = jwtUtil.generateToken(123L, "test@example.com", "Test User", Role.TECHNICIAN, false);
        
        Long userId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));
        assertEquals(123L, userId);
    }
    
    @Test
    void testTokenContainsName() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "John Doe", Role.DISPATCHER, false);
        
        String name = jwtUtil.extractClaim(token, claims -> claims.get("name", String.class));
        assertEquals("John Doe", name);
    }
    
    @Test
    void testTokenContainsRole() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.SUPERVISOR, false);
        
        String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("SUPERVISOR", role);
    }
    
    @Test
    void testExtractRole() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.DISPATCHER, false);
        
        String role = jwtUtil.extractRole(token);
        assertEquals("DISPATCHER", role);
    }
    
    @Test
    void testExtractRoleForAllRoles() {
        Role[] roles = {Role.ADMIN, Role.DISPATCHER, Role.SUPERVISOR, Role.TECHNICIAN};
        
        for (Role expectedRole : roles) {
            String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", expectedRole, false);
            String extractedRole = jwtUtil.extractRole(token);
            
            assertEquals(expectedRole.name(), extractedRole);
        }
    }
    
    @Test
    void testWebTokenExpiration() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, false);
        
        long expiration = jwtUtil.extractExpiration(token).getTime();
        long now = System.currentTimeMillis();
        long difference = expiration - now;
        
        // Should be around 24 hours (86400000 ms), allow 10 second tolerance
        assertTrue(difference >= 86390000 && difference <= 86400000);
    }
    
    @Test
    void testMobileTokenExpiration() {
        String token = jwtUtil.generateToken(1L, "test@example.com", "Test User", Role.ADMIN, true);
        
        long expiration = jwtUtil.extractExpiration(token).getTime();
        long now = System.currentTimeMillis();
        long difference = expiration - now;
        
        // Should be around 30 days (2592000000 ms), allow 10 second tolerance
        assertTrue(difference >= 2591990000L && difference <= 2592000000L);
    }
}
