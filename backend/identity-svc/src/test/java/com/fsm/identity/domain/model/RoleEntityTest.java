package com.fsm.identity.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoleEntity
 */
class RoleEntityTest {
    
    @Test
    void testRoleEntityBuilderCreatesValidEntity() {
        RoleEntity role = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .description("Administrator role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        assertNotNull(role);
        assertEquals(1L, role.getId());
        assertEquals(Role.ADMIN, role.getName());
        assertEquals("Administrator role", role.getDescription());
        assertNotNull(role.getCreatedAt());
        assertNotNull(role.getUpdatedAt());
    }
    
    @Test
    void testRoleEntityWithAllRoleTypes() {
        Role[] roles = Role.values();
        
        for (Role roleType : roles) {
            RoleEntity role = RoleEntity.builder()
                    .name(roleType)
                    .description(roleType.getDescription())
                    .build();
            
            assertNotNull(role);
            assertEquals(roleType, role.getName());
            assertNotNull(role.getDescription());
        }
    }
    
    @Test
    void testRoleEntityNoArgsConstructor() {
        RoleEntity role = new RoleEntity();
        
        assertNotNull(role);
        assertNull(role.getId());
        assertNull(role.getName());
        assertNull(role.getDescription());
    }
    
    @Test
    void testRoleEntityAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        RoleEntity role = new RoleEntity(1L, Role.ADMIN, "Admin role", now, now);
        
        assertNotNull(role);
        assertEquals(1L, role.getId());
        assertEquals(Role.ADMIN, role.getName());
        assertEquals("Admin role", role.getDescription());
        assertEquals(now, role.getCreatedAt());
        assertEquals(now, role.getUpdatedAt());
    }
    
    @Test
    void testRoleEntitySetters() {
        RoleEntity role = new RoleEntity();
        LocalDateTime now = LocalDateTime.now();
        
        role.setId(2L);
        role.setName(Role.TECHNICIAN);
        role.setDescription("Tech role");
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        
        assertEquals(2L, role.getId());
        assertEquals(Role.TECHNICIAN, role.getName());
        assertEquals("Tech role", role.getDescription());
        assertEquals(now, role.getCreatedAt());
        assertEquals(now, role.getUpdatedAt());
    }
    
    @Test
    void testRoleEntityGetters() {
        LocalDateTime now = LocalDateTime.now();
        RoleEntity role = RoleEntity.builder()
                .id(3L)
                .name(Role.DISPATCHER)
                .description("Dispatcher role")
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        assertEquals(3L, role.getId());
        assertEquals(Role.DISPATCHER, role.getName());
        assertEquals("Dispatcher role", role.getDescription());
        assertEquals(now, role.getCreatedAt());
        assertEquals(now, role.getUpdatedAt());
    }
    
    @Test
    void testRoleEntityEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        RoleEntity role1 = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .description("Admin")
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        RoleEntity role2 = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .description("Admin")
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }
    
    @Test
    void testRoleEntityToString() {
        RoleEntity role = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .description("Admin role")
                .build();
        
        String toString = role.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ADMIN"));
    }
    
    @Test
    void testRoleEntityPrePersist() {
        RoleEntity role = new RoleEntity();
        
        assertNull(role.getCreatedAt());
        assertNull(role.getUpdatedAt());
        
        role.onCreate();
        
        assertNotNull(role.getCreatedAt());
        assertNotNull(role.getUpdatedAt());
    }
    
    @Test
    void testRoleEntityPreUpdate() {
        RoleEntity role = new RoleEntity();
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        role.setCreatedAt(created);
        role.setUpdatedAt(created);
        
        // Simulate update
        try {
            Thread.sleep(10); // Small delay to ensure different timestamp
        } catch (InterruptedException e) {
            // Ignore
        }
        
        role.onUpdate();
        
        assertEquals(created, role.getCreatedAt()); // createdAt shouldn't change
        assertNotNull(role.getUpdatedAt());
        assertTrue(role.getUpdatedAt().isAfter(created)); // updatedAt should be newer
    }
    
    @Test
    void testRoleEntityEqualsWithNull() {
        RoleEntity role = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .build();
        
        assertNotEquals(role, null);
    }
    
    @Test
    void testRoleEntityEqualsWithDifferentClass() {
        RoleEntity role = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .build();
        
        assertNotEquals(role, "not a role entity");
    }
    
    @Test
    void testRoleEntityEqualsSameObject() {
        RoleEntity role = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .build();
        
        assertEquals(role, role);
    }
    
    @Test
    void testUserWithDescription() {
        String description = "Custom admin description";
        RoleEntity role = RoleEntity.builder()
                .name(Role.ADMIN)
                .description(description)
                .build();
        
        assertEquals(description, role.getDescription());
    }
    
    @Test
    void testRoleEntityBuilder() {
        RoleEntity role = RoleEntity.builder().build();
        
        assertNotNull(role);
    }
    
    @Test
    void testRoleEntityCanEqual() {
        RoleEntity role1 = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .build();
        
        RoleEntity role2 = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .build();
        
        assertTrue(role1.canEqual(role2));
        assertTrue(role2.canEqual(role1));
    }
    
    @Test
    void testRoleEntityBuilderWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        RoleEntity role = RoleEntity.builder()
                .id(5L)
                .name(Role.SUPERVISOR)
                .description("Supervisor with all fields")
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        assertEquals(5L, role.getId());
        assertEquals(Role.SUPERVISOR, role.getName());
        assertEquals("Supervisor with all fields", role.getDescription());
        assertEquals(now, role.getCreatedAt());
        assertEquals(now, role.getUpdatedAt());
    }
    
    @Test
    void testRoleEntityBuilderToString() {
        RoleEntity.RoleEntityBuilder builder = RoleEntity.builder()
                .name(Role.TECHNICIAN)
                .description("Builder test");
        
        String builderStr = builder.toString();
        assertNotNull(builderStr);
        // Lombok builder toString should contain the builder class name
        assertTrue(builderStr.contains("RoleEntity.RoleEntityBuilder") || builderStr.contains("RoleEntityBuilder"));
    }
    
    @Test
    void testRoleEntityNotEqualsWithDifferentId() {
        RoleEntity role1 = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .build();
        
        RoleEntity role2 = RoleEntity.builder()
                .id(2L)
                .name(Role.ADMIN)
                .build();
        
        assertNotEquals(role1, role2);
    }
    
    @Test
    void testRoleEntityNotEqualsWithDifferentName() {
        RoleEntity role1 = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .build();
        
        RoleEntity role2 = RoleEntity.builder()
                .id(1L)
                .name(Role.TECHNICIAN)
                .build();
        
        assertNotEquals(role1, role2);
    }
    
    @Test
    void testRoleEntityHashCodeConsistency() {
        RoleEntity role = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .description("Admin")
                .build();
        
        int hashCode1 = role.hashCode();
        int hashCode2 = role.hashCode();
        
        assertEquals(hashCode1, hashCode2);
    }
}
