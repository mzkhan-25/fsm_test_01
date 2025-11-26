package com.fsm.identity.domain.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 */
class UserTest {
    
    private Validator validator;
    private RoleEntity technicianRole;
    private RoleEntity adminRole;
    private RoleEntity dispatcherRole;
    private RoleEntity supervisorRole;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create role entities for testing
        technicianRole = RoleEntity.builder()
                .id(1L)
                .name(Role.TECHNICIAN)
                .description("Technician role")
                .build();
        
        adminRole = RoleEntity.builder()
                .id(2L)
                .name(Role.ADMIN)
                .description("Admin role")
                .build();
        
        dispatcherRole = RoleEntity.builder()
                .id(3L)
                .name(Role.DISPATCHER)
                .description("Dispatcher role")
                .build();
        
        supervisorRole = RoleEntity.builder()
                .id(4L)
                .name(Role.SUPERVISOR)
                .description("Supervisor role")
                .build();
    }
    
    @Test
    void testUserBuilderCreatesValidUser() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+12025551234")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("+12025551234", user.getPhone());
        assertEquals(technicianRole, user.getRole());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }
    
    @Test
    void testUserDefaultStatus() {
        User user = User.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .password("hashedPassword123")
                .role(dispatcherRole)
                .build();
        
        assertEquals(User.UserStatus.ACTIVE, user.getStatus(), 
                "Default status should be ACTIVE");
    }
    
    @Test
    void testUserActivateMethod() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.INACTIVE)
                .build();
        
        assertFalse(user.isActive());
        user.activate();
        assertTrue(user.isActive());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }
    
    @Test
    void testUserDeactivateMethod() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        assertTrue(user.isActive());
        user.deactivate();
        assertFalse(user.isActive());
        assertEquals(User.UserStatus.INACTIVE, user.getStatus());
    }
    
    @Test
    void testUserIsActiveMethod() {
        User activeUser = User.builder()
                .name("Active User")
                .email("active@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        User inactiveUser = User.builder()
                .name("Inactive User")
                .email("inactive@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.INACTIVE)
                .build();
        
        assertTrue(activeUser.isActive());
        assertFalse(inactiveUser.isActive());
    }
    
    @Test
    void testValidationWithBlankName() {
        User user = User.builder()
                .name("")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
    
    @Test
    void testValidationWithNullName() {
        User user = User.builder()
                .name(null)
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
    
    @Test
    void testValidationWithInvalidEmail() {
        User user = User.builder()
                .name("Test User")
                .email("invalid-email")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
    
    @Test
    void testValidationWithBlankEmail() {
        User user = User.builder()
                .name("Test User")
                .email("")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
    
    @Test
    void testValidationWithNullRole() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(null)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }
    
    @Test
    void testValidationWithNullStatus() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(null)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }
    
    @Test
    void testValidationWithInvalidPhoneFormat() {
        String[] invalidPhones = {
            "abc123",        // Contains letters
            "+",             // Just a plus sign
            "0123456789"     // Starts with 0 without +
        };
        
        for (String phone : invalidPhones) {
            User user = User.builder()
                    .name("Test User")
                    .email("test@example.com")
                .password("hashedPassword123")
                    .phone(phone)
                    .role(technicianRole)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), 
                    "Phone " + phone + " should be invalid");
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("phone")),
                    "Phone " + phone + " should have validation error");
        }
    }
    
    @Test
    void testValidationWithValidPhoneFormats() {
        String[] validPhones = {
            "+12025551234",
            "+442071234567",
            "+919876543210"
        };
        
        for (String phone : validPhones) {
            User user = User.builder()
                    .name("Test User")
                    .email("test@example.com")
                .password("hashedPassword123")
                    .phone(phone)
                    .role(technicianRole)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertTrue(violations.isEmpty() || violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("phone")),
                    "Phone " + phone + " should be valid");
        }
    }
    
    @Test
    void testValidationWithNullPhone() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword123")
                .phone(null)
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        // Phone is optional, so null should be valid
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }
    
    @Test
    void testUserWithAllRoles() {
        RoleEntity[] roles = {technicianRole, adminRole, dispatcherRole, supervisorRole};
        
        for (RoleEntity role : roles) {
            User user = User.builder()
                    .name("Test User")
                    .email("test@example.com")
                .password("hashedPassword123")
                    .role(role)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            
            assertEquals(role, user.getRole());
        }
    }
    
    @Test
    void testUserStatusEnum() {
        assertEquals(2, User.UserStatus.values().length, 
                "Should have exactly 2 status values");
        assertNotNull(User.UserStatus.ACTIVE);
        assertNotNull(User.UserStatus.INACTIVE);
    }
    
    @Test
    void testUserEqualsAndHashCode() {
        User user1 = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        User user2 = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
    
    @Test
    void testUserToString() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        String toString = user.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("John Doe"));
        assertTrue(toString.contains("john@example.com"));
    }
    
    @Test
    void testUserEqualsWithNull() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .build();
        
        assertNotEquals(user, null);
    }
    
    @Test
    void testUserEqualsWithDifferentClass() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .build();
        
        assertNotEquals(user, "not a user");
    }
    
    @Test
    void testUserEqualsSameObject() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .build();
        
        assertEquals(user, user);
    }
    
    @Test
    void testUserAllArgsConstructor() {
        User user = new User(1L, "John", "john@example.com", "+12025551000",
                "hashedPassword123", technicianRole, User.UserStatus.ACTIVE, null,
                LocalDateTime.now(), LocalDateTime.now());
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John", user.getName());
    }
    
    @Test
    void testUserSetEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        
        assertEquals("test@example.com", user.getEmail());
    }
    
    @Test
    void testUserSetPhone() {
        User user = new User();
        user.setPhone("+12025551234");
        
        assertEquals("+12025551234", user.getPhone());
    }
    
    @Test
    void testUserSetRole() {
        User user = new User();
        user.setRole(adminRole);
        
        assertEquals(adminRole, user.getRole());
    }
    
    @Test
    void testUserSetId() {
        User user = new User();
        user.setId(99L);
        
        assertEquals(99L, user.getId());
    }
    
    @Test
    void testUserSetCreatedAt() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        
        assertEquals(now, user.getCreatedAt());
    }
    
    @Test
    void testUserSetUpdatedAt() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        user.setUpdatedAt(now);
        
        assertEquals(now, user.getUpdatedAt());
    }
    
    @Test
    void testUserOnUpdate() {
        User user = new User();
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        user.setCreatedAt(created);
        user.setUpdatedAt(created);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }
        
        user.onUpdate();
        
        assertEquals(created, user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(created));
    }
    
    @Test
    void testUserBuilder() {
        User user = User.builder().build();
        
        assertNotNull(user);
    }
    
    @Test
    void testUserCanEqual() {
        User user1 = User.builder()
                .id(1L)
                .name("Test")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .build();
        
        User user2 = User.builder()
                .id(1L)
                .name("Test")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(technicianRole)
                .build();
        
        assertTrue(user1.canEqual(user2));
        assertTrue(user2.canEqual(user1));
    }
    
    @Test
    void testUserOnCreate() {
        User user = new User();
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        
        user.onCreate();
        
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }
    
    @Test
    void testUserSetName() {
        User user = new User();
        user.setName("Test Name");
        
        assertEquals("Test Name", user.getName());
    }
    
    @Test
    void testUserSetStatus() {
        User user = new User();
        user.setStatus(User.UserStatus.INACTIVE);
        
        assertEquals(User.UserStatus.INACTIVE, user.getStatus());
    }
    
    @Test
    void testUserBuilderWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(10L)
                .name("Complete User")
                .email("complete@example.com")
                .phone("+15555551234")
                .role(adminRole)
                .status(User.UserStatus.INACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        assertEquals(10L, user.getId());
        assertEquals("Complete User", user.getName());
        assertEquals("complete@example.com", user.getEmail());
        assertEquals("+15555551234", user.getPhone());
        assertEquals(adminRole, user.getRole());
        assertEquals(User.UserStatus.INACTIVE, user.getStatus());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
    
    @Test
    void testUserBuilderToString() {
        User.UserBuilder builder = User.builder()
                .name("Builder Test")
                .email("builder@example.com");
        
        String builderStr = builder.toString();
        assertNotNull(builderStr);
        // Lombok builder toString should contain the builder class name
        assertTrue(builderStr.contains("User.UserBuilder") || builderStr.contains("UserBuilder"));
    }
    
    @Test
    void testUserNotEqualsWithDifferentId() {
        User user1 = User.builder()
                .id(1L)
                .name("Test")
                .email("test@example.com")
                .role(technicianRole)
                .build();
        
        User user2 = User.builder()
                .id(2L)
                .name("Test")
                .email("test@example.com")
                .role(technicianRole)
                .build();
        
        assertNotEquals(user1, user2);
    }
    
    @Test
    void testUserNotEqualsWithDifferentEmail() {
        User user1 = User.builder()
                .id(1L)
                .name("Test")
                .email("test1@example.com")
                .role(technicianRole)
                .build();
        
        User user2 = User.builder()
                .id(1L)
                .name("Test")
                .email("test2@example.com")
                .role(technicianRole)
                .build();
        
        assertNotEquals(user1, user2);
    }
    
    @Test
    void testRegisterDeviceToken() {
        User user = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(technicianRole)
                .build();
        
        assertNull(user.getDeviceToken());
        
        String token = "fcm_test_token_123456";
        user.registerDeviceToken(token);
        
        assertEquals(token, user.getDeviceToken());
    }
    
    @Test
    void testRegisterDeviceTokenOverridesPrevious() {
        User user = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(technicianRole)
                .deviceToken("old_token")
                .build();
        
        assertEquals("old_token", user.getDeviceToken());
        
        String newToken = "new_fcm_token_789";
        user.registerDeviceToken(newToken);
        
        assertEquals(newToken, user.getDeviceToken());
    }
    
    @Test
    void testDeviceTokenInBuilder() {
        String token = "fcm_token_from_builder";
        
        User user = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(technicianRole)
                .deviceToken(token)
                .build();
        
        assertEquals(token, user.getDeviceToken());
    }
}
