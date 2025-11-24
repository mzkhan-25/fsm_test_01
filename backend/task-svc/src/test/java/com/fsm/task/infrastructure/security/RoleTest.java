package com.fsm.task.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Role enum
 */
class RoleTest {
    
    @Test
    void testAllRolesExist() {
        assertEquals(4, Role.values().length);
        assertNotNull(Role.ADMIN);
        assertNotNull(Role.DISPATCHER);
        assertNotNull(Role.SUPERVISOR);
        assertNotNull(Role.TECHNICIAN);
    }
    
    @Test
    void testRoleValueOf() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.DISPATCHER, Role.valueOf("DISPATCHER"));
        assertEquals(Role.SUPERVISOR, Role.valueOf("SUPERVISOR"));
        assertEquals(Role.TECHNICIAN, Role.valueOf("TECHNICIAN"));
    }
    
    @Test
    void testRoleName() {
        assertEquals("ADMIN", Role.ADMIN.name());
        assertEquals("DISPATCHER", Role.DISPATCHER.name());
        assertEquals("SUPERVISOR", Role.SUPERVISOR.name());
        assertEquals("TECHNICIAN", Role.TECHNICIAN.name());
    }
}
