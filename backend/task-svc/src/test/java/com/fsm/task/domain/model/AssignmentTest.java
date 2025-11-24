package com.fsm.task.domain.model;

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
 * Unit tests for Assignment entity
 */
class AssignmentTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testAssignmentBuilderCreatesValidAssignment() {
        LocalDateTime now = LocalDateTime.now();
        Assignment assignment = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(now)
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        assertNotNull(assignment);
        assertEquals(1L, assignment.getId());
        assertEquals(10L, assignment.getTaskId());
        assertEquals(101L, assignment.getTechnicianId());
        assertEquals(now, assignment.getAssignedAt());
        assertEquals("dispatcher@fsm.com", assignment.getAssignedBy());
        assertEquals(Assignment.AssignmentStatus.ACTIVE, assignment.getStatus());
    }
    
    @Test
    void testAssignmentDefaultStatus() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        assertEquals(Assignment.AssignmentStatus.ACTIVE, assignment.getStatus(),
                "Default status should be ACTIVE");
    }
    
    @Test
    void testValidationWithNullTaskId() {
        Assignment assignment = Assignment.builder()
                .taskId(null)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("taskId")));
    }
    
    @Test
    void testValidationWithNullTechnicianId() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(null)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("technicianId")));
    }
    
    @Test
    void testValidationWithNullAssignedAt() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(null)
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("assignedAt")));
    }
    
    @Test
    void testValidationWithNullAssignedBy() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy(null)
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("assignedBy")));
    }
    
    @Test
    void testValidationWithNullStatus() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(null)
                .build();
        
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }
    
    @Test
    void testValidationWithValidAssignment() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertTrue(violations.isEmpty(), "Valid assignment should have no violations");
    }
    
    @Test
    void testAssignmentStatusEnum() {
        assertEquals(4, Assignment.AssignmentStatus.values().length,
                "Should have exactly 4 status values");
        assertNotNull(Assignment.AssignmentStatus.ACTIVE);
        assertNotNull(Assignment.AssignmentStatus.REASSIGNED);
        assertNotNull(Assignment.AssignmentStatus.COMPLETED);
        assertNotNull(Assignment.AssignmentStatus.CANCELLED);
    }
    
    @Test
    void testMarkAsReassignedFromActive() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        assertTrue(assignment.isActive());
        assignment.markAsReassigned("Technician unavailable");
        assertEquals(Assignment.AssignmentStatus.REASSIGNED, assignment.getStatus());
        assertEquals("Technician unavailable", assignment.getReason());
    }
    
    @Test
    void testMarkAsReassignedFromNonActive() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.COMPLETED)
                .build();
        
        assignment.markAsReassigned("Technician unavailable");
        // Should not change status if not active
        assertEquals(Assignment.AssignmentStatus.COMPLETED, assignment.getStatus());
    }
    
    @Test
    void testCompleteFromActive() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        assertFalse(assignment.isCompleted());
        assignment.complete();
        assertTrue(assignment.isCompleted());
        assertEquals(Assignment.AssignmentStatus.COMPLETED, assignment.getStatus());
    }
    
    @Test
    void testCompleteFromNonActive() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.REASSIGNED)
                .build();
        
        assignment.complete();
        // Should not change status if not active
        assertEquals(Assignment.AssignmentStatus.REASSIGNED, assignment.getStatus());
    }
    
    @Test
    void testCancelFromActive() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        assignment.cancel("Customer cancelled");
        assertEquals(Assignment.AssignmentStatus.CANCELLED, assignment.getStatus());
        assertEquals("Customer cancelled", assignment.getReason());
    }
    
    @Test
    void testCancelFromNonActive() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.COMPLETED)
                .build();
        
        assignment.cancel("Customer cancelled");
        // Should not change status if not active
        assertEquals(Assignment.AssignmentStatus.COMPLETED, assignment.getStatus());
    }
    
    @Test
    void testIsActiveMethod() {
        Assignment activeAssignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        Assignment completedAssignment = Assignment.builder()
                .taskId(11L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.COMPLETED)
                .build();
        
        assertTrue(activeAssignment.isActive());
        assertFalse(completedAssignment.isActive());
    }
    
    @Test
    void testIsCompletedMethod() {
        Assignment completedAssignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.COMPLETED)
                .build();
        
        Assignment activeAssignment = Assignment.builder()
                .taskId(11L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        assertTrue(completedAssignment.isCompleted());
        assertFalse(activeAssignment.isCompleted());
    }
    
    @Test
    void testAssignmentWithReason() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.REASSIGNED)
                .reason("Original technician on leave")
                .build();
        
        assertEquals("Original technician on leave", assignment.getReason());
    }
    
    @Test
    void testAssignmentEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Assignment assignment1 = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(now)
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(now)
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        assertEquals(assignment1, assignment2);
        assertEquals(assignment1.hashCode(), assignment2.hashCode());
    }
    
    @Test
    void testAssignmentToString() {
        Assignment assignment = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        String toString = assignment.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("10") || toString.contains("taskId"));
    }
    
    @Test
    void testAssignmentEqualsWithNull() {
        Assignment assignment = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        assertNotEquals(assignment, null);
    }
    
    @Test
    void testAssignmentEqualsWithDifferentClass() {
        Assignment assignment = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        assertNotEquals(assignment, "not an assignment");
    }
    
    @Test
    void testAssignmentEqualsSameObject() {
        Assignment assignment = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        assertEquals(assignment, assignment);
    }
    
    @Test
    void testAssignmentNotEqualsWithDifferentId() {
        LocalDateTime now = LocalDateTime.now();
        Assignment assignment1 = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(now)
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .id(2L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(now)
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        assertNotEquals(assignment1, assignment2);
    }
    
    @Test
    void testAssignmentNotEqualsWithDifferentTaskId() {
        LocalDateTime now = LocalDateTime.now();
        Assignment assignment1 = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(now)
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .id(1L)
                .taskId(20L)
                .technicianId(101L)
                .assignedAt(now)
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        assertNotEquals(assignment1, assignment2);
    }
    
    @Test
    void testAssignmentAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Assignment assignment = new Assignment(1L, 10L, 101L, now, 
                "dispatcher@fsm.com", Assignment.AssignmentStatus.ACTIVE, "test reason");
        
        assertNotNull(assignment);
        assertEquals(1L, assignment.getId());
        assertEquals(10L, assignment.getTaskId());
        assertEquals(101L, assignment.getTechnicianId());
        assertEquals(now, assignment.getAssignedAt());
        assertEquals("dispatcher@fsm.com", assignment.getAssignedBy());
        assertEquals(Assignment.AssignmentStatus.ACTIVE, assignment.getStatus());
        assertEquals("test reason", assignment.getReason());
    }
    
    @Test
    void testAssignmentNoArgsConstructor() {
        Assignment assignment = new Assignment();
        assertNotNull(assignment);
    }
    
    @Test
    void testAssignmentSetters() {
        Assignment assignment = new Assignment();
        LocalDateTime now = LocalDateTime.now();
        
        assignment.setId(1L);
        assignment.setTaskId(10L);
        assignment.setTechnicianId(101L);
        assignment.setAssignedAt(now);
        assignment.setAssignedBy("dispatcher@fsm.com");
        assignment.setStatus(Assignment.AssignmentStatus.ACTIVE);
        assignment.setReason("test reason");
        
        assertEquals(1L, assignment.getId());
        assertEquals(10L, assignment.getTaskId());
        assertEquals(101L, assignment.getTechnicianId());
        assertEquals(now, assignment.getAssignedAt());
        assertEquals("dispatcher@fsm.com", assignment.getAssignedBy());
        assertEquals(Assignment.AssignmentStatus.ACTIVE, assignment.getStatus());
        assertEquals("test reason", assignment.getReason());
    }
    
    @Test
    void testAssignmentOnCreate() {
        Assignment assignment = new Assignment();
        assertNull(assignment.getAssignedAt());
        
        assignment.onCreate();
        
        assertNotNull(assignment.getAssignedAt());
    }
    
    @Test
    void testAssignmentOnCreateDoesNotOverwrite() {
        LocalDateTime specificTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        Assignment assignment = new Assignment();
        assignment.setAssignedAt(specificTime);
        
        assignment.onCreate();
        
        // Should not overwrite existing assignedAt
        assertEquals(specificTime, assignment.getAssignedAt());
    }
    
    @Test
    void testAssignmentBuilder() {
        Assignment assignment = Assignment.builder().build();
        
        assertNotNull(assignment);
    }
    
    @Test
    void testAssignmentCanEqual() {
        Assignment assignment1 = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .build();
        
        assertTrue(assignment1.canEqual(assignment2));
        assertTrue(assignment2.canEqual(assignment1));
    }
    
    @Test
    void testAssignmentBuilderToString() {
        Assignment.AssignmentBuilder builder = Assignment.builder()
                .taskId(10L)
                .technicianId(101L);
        
        String builderStr = builder.toString();
        assertNotNull(builderStr);
        assertTrue(builderStr.contains("Assignment") || builderStr.contains("AssignmentBuilder"));
    }
    
    @Test
    void testAssignmentBuilderWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Assignment assignment = Assignment.builder()
                .id(1L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(now)
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.COMPLETED)
                .reason("Task finished")
                .build();
        
        assertEquals(1L, assignment.getId());
        assertEquals(10L, assignment.getTaskId());
        assertEquals(101L, assignment.getTechnicianId());
        assertEquals(now, assignment.getAssignedAt());
        assertEquals("dispatcher@fsm.com", assignment.getAssignedBy());
        assertEquals(Assignment.AssignmentStatus.COMPLETED, assignment.getStatus());
        assertEquals("Task finished", assignment.getReason());
    }
    
    @Test
    void testAllAssignmentStatuses() {
        Assignment.AssignmentStatus[] statuses = {
            Assignment.AssignmentStatus.ACTIVE,
            Assignment.AssignmentStatus.REASSIGNED,
            Assignment.AssignmentStatus.COMPLETED,
            Assignment.AssignmentStatus.CANCELLED
        };
        
        for (Assignment.AssignmentStatus status : statuses) {
            Assignment assignment = Assignment.builder()
                    .taskId(10L)
                    .technicianId(101L)
                    .assignedAt(LocalDateTime.now())
                    .assignedBy("dispatcher@fsm.com")
                    .status(status)
                    .build();
            
            assertEquals(status, assignment.getStatus());
        }
    }
}
