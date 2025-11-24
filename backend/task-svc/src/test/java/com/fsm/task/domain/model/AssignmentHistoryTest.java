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
 * Unit tests for AssignmentHistory entity
 */
class AssignmentHistoryTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testAssignmentHistoryBuilderCreatesValidHistory() {
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory history = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        assertNotNull(history);
        assertEquals(1L, history.getId());
        assertEquals(100L, history.getAssignmentId());
        assertEquals(10L, history.getTaskId());
        assertEquals(101L, history.getTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.CREATED, history.getAction());
        assertEquals("dispatcher@fsm.com", history.getActionBy());
        assertEquals(now, history.getActionAt());
    }
    
    @Test
    void testValidationWithNullAssignmentId() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(null)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(history);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("assignmentId")));
    }
    
    @Test
    void testValidationWithNullTaskId() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(null)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(history);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("taskId")));
    }
    
    @Test
    void testValidationWithNullTechnicianId() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(null)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(history);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("technicianId")));
    }
    
    @Test
    void testValidationWithNullAction() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(null)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(history);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("action")));
    }
    
    @Test
    void testValidationWithNullActionBy() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy(null)
                .actionAt(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(history);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("actionBy")));
    }
    
    @Test
    void testValidationWithBlankActionBy() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("")
                .actionAt(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(history);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("actionBy")));
    }
    
    @Test
    void testValidationWithNullActionAt() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(null)
                .build();
        
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(history);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("actionAt")));
    }
    
    @Test
    void testValidationWithValidHistory() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<AssignmentHistory>> violations = validator.validate(history);
        assertTrue(violations.isEmpty(), "Valid history should have no violations");
    }
    
    @Test
    void testHistoryActionEnum() {
        assertEquals(4, AssignmentHistory.HistoryAction.values().length,
                "Should have exactly 4 action values");
        assertNotNull(AssignmentHistory.HistoryAction.CREATED);
        assertNotNull(AssignmentHistory.HistoryAction.REASSIGNED);
        assertNotNull(AssignmentHistory.HistoryAction.COMPLETED);
        assertNotNull(AssignmentHistory.HistoryAction.CANCELLED);
    }
    
    @Test
    void testForCreationFactory() {
        Assignment assignment = Assignment.builder()
                .id(100L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        AssignmentHistory history = AssignmentHistory.forCreation(assignment, "dispatcher@fsm.com");
        
        assertNotNull(history);
        assertEquals(100L, history.getAssignmentId());
        assertEquals(10L, history.getTaskId());
        assertEquals(101L, history.getTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.CREATED, history.getAction());
        assertEquals("dispatcher@fsm.com", history.getActionBy());
        assertNotNull(history.getActionAt());
        assertNull(history.getPreviousTechnicianId());
        assertNull(history.getReason());
    }
    
    @Test
    void testForReassignmentFactory() {
        Assignment assignment = Assignment.builder()
                .id(100L)
                .taskId(10L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.ACTIVE)
                .build();
        
        AssignmentHistory history = AssignmentHistory.forReassignment(
                assignment, 101L, "dispatcher@fsm.com", "Original technician unavailable");
        
        assertNotNull(history);
        assertEquals(100L, history.getAssignmentId());
        assertEquals(10L, history.getTaskId());
        assertEquals(102L, history.getTechnicianId());
        assertEquals(101L, history.getPreviousTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.REASSIGNED, history.getAction());
        assertEquals("dispatcher@fsm.com", history.getActionBy());
        assertEquals("Original technician unavailable", history.getReason());
        assertNotNull(history.getActionAt());
    }
    
    @Test
    void testForCompletionFactory() {
        Assignment assignment = Assignment.builder()
                .id(100L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.COMPLETED)
                .build();
        
        AssignmentHistory history = AssignmentHistory.forCompletion(assignment, "technician@fsm.com");
        
        assertNotNull(history);
        assertEquals(100L, history.getAssignmentId());
        assertEquals(10L, history.getTaskId());
        assertEquals(101L, history.getTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.COMPLETED, history.getAction());
        assertEquals("technician@fsm.com", history.getActionBy());
        assertNotNull(history.getActionAt());
        assertNull(history.getPreviousTechnicianId());
        assertNull(history.getReason());
    }
    
    @Test
    void testForCancellationFactory() {
        Assignment assignment = Assignment.builder()
                .id(100L)
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(Assignment.AssignmentStatus.CANCELLED)
                .build();
        
        AssignmentHistory history = AssignmentHistory.forCancellation(
                assignment, "dispatcher@fsm.com", "Customer cancelled the task");
        
        assertNotNull(history);
        assertEquals(100L, history.getAssignmentId());
        assertEquals(10L, history.getTaskId());
        assertEquals(101L, history.getTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.CANCELLED, history.getAction());
        assertEquals("dispatcher@fsm.com", history.getActionBy());
        assertEquals("Customer cancelled the task", history.getReason());
        assertNotNull(history.getActionAt());
        assertNull(history.getPreviousTechnicianId());
    }
    
    @Test
    void testHistoryWithPreviousTechnicianId() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(102L)
                .previousTechnicianId(101L)
                .action(AssignmentHistory.HistoryAction.REASSIGNED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .reason("Reassigned to another technician")
                .build();
        
        assertEquals(102L, history.getTechnicianId());
        assertEquals(101L, history.getPreviousTechnicianId());
        assertEquals("Reassigned to another technician", history.getReason());
    }
    
    @Test
    void testHistoryEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory history1 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        AssignmentHistory history2 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        assertEquals(history1, history2);
        assertEquals(history1.hashCode(), history2.hashCode());
    }
    
    @Test
    void testHistoryToString() {
        AssignmentHistory history = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        String toString = history.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("100") || toString.contains("assignmentId"));
    }
    
    @Test
    void testHistoryEqualsWithNull() {
        AssignmentHistory history = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        assertNotEquals(history, null);
    }
    
    @Test
    void testHistoryEqualsWithDifferentClass() {
        AssignmentHistory history = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        assertNotEquals(history, "not a history");
    }
    
    @Test
    void testHistoryEqualsSameObject() {
        AssignmentHistory history = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        assertEquals(history, history);
    }
    
    @Test
    void testHistoryNotEqualsWithDifferentId() {
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory history1 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        AssignmentHistory history2 = AssignmentHistory.builder()
                .id(2L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        assertNotEquals(history1, history2);
    }
    
    @Test
    void testHistoryAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory history = new AssignmentHistory(1L, 100L, 10L, 101L, 
                102L, AssignmentHistory.HistoryAction.REASSIGNED, 
                "dispatcher@fsm.com", now, "test reason");
        
        assertNotNull(history);
        assertEquals(1L, history.getId());
        assertEquals(100L, history.getAssignmentId());
        assertEquals(10L, history.getTaskId());
        assertEquals(101L, history.getTechnicianId());
        assertEquals(102L, history.getPreviousTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.REASSIGNED, history.getAction());
        assertEquals("dispatcher@fsm.com", history.getActionBy());
        assertEquals(now, history.getActionAt());
        assertEquals("test reason", history.getReason());
    }
    
    @Test
    void testHistoryNoArgsConstructor() {
        AssignmentHistory history = new AssignmentHistory();
        assertNotNull(history);
    }
    
    @Test
    void testHistorySetters() {
        AssignmentHistory history = new AssignmentHistory();
        LocalDateTime now = LocalDateTime.now();
        
        history.setId(1L);
        history.setAssignmentId(100L);
        history.setTaskId(10L);
        history.setTechnicianId(101L);
        history.setPreviousTechnicianId(102L);
        history.setAction(AssignmentHistory.HistoryAction.COMPLETED);
        history.setActionBy("dispatcher@fsm.com");
        history.setActionAt(now);
        history.setReason("test reason");
        
        assertEquals(1L, history.getId());
        assertEquals(100L, history.getAssignmentId());
        assertEquals(10L, history.getTaskId());
        assertEquals(101L, history.getTechnicianId());
        assertEquals(102L, history.getPreviousTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.COMPLETED, history.getAction());
        assertEquals("dispatcher@fsm.com", history.getActionBy());
        assertEquals(now, history.getActionAt());
        assertEquals("test reason", history.getReason());
    }
    
    @Test
    void testHistoryOnCreate() {
        AssignmentHistory history = new AssignmentHistory();
        assertNull(history.getActionAt());
        
        history.onCreate();
        
        assertNotNull(history.getActionAt());
    }
    
    @Test
    void testHistoryOnCreateDoesNotOverwrite() {
        LocalDateTime specificTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        AssignmentHistory history = new AssignmentHistory();
        history.setActionAt(specificTime);
        
        history.onCreate();
        
        // Should not overwrite existing actionAt
        assertEquals(specificTime, history.getActionAt());
    }
    
    @Test
    void testHistoryBuilder() {
        AssignmentHistory history = AssignmentHistory.builder().build();
        assertNotNull(history);
    }
    
    @Test
    void testAllHistoryActions() {
        AssignmentHistory.HistoryAction[] actions = {
            AssignmentHistory.HistoryAction.CREATED,
            AssignmentHistory.HistoryAction.REASSIGNED,
            AssignmentHistory.HistoryAction.COMPLETED,
            AssignmentHistory.HistoryAction.CANCELLED
        };
        
        for (AssignmentHistory.HistoryAction action : actions) {
            AssignmentHistory history = AssignmentHistory.builder()
                    .assignmentId(100L)
                    .taskId(10L)
                    .technicianId(101L)
                    .action(action)
                    .actionBy("dispatcher@fsm.com")
                    .actionAt(LocalDateTime.now())
                    .build();
            
            assertEquals(action, history.getAction());
        }
    }
    
    @Test
    void testHistoryCanEqual() {
        AssignmentHistory history1 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        AssignmentHistory history2 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        assertTrue(history1.canEqual(history2));
        assertTrue(history2.canEqual(history1));
    }
    
    @Test
    void testHistoryBuilderToString() {
        AssignmentHistory.AssignmentHistoryBuilder builder = AssignmentHistory.builder()
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L);
        
        String builderStr = builder.toString();
        assertNotNull(builderStr);
        assertTrue(builderStr.contains("AssignmentHistory") || builderStr.contains("AssignmentHistoryBuilder"));
    }
    
    @Test
    void testHistoryBuilderWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory history = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .previousTechnicianId(102L)
                .action(AssignmentHistory.HistoryAction.REASSIGNED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .reason("Test reason")
                .build();
        
        assertEquals(1L, history.getId());
        assertEquals(100L, history.getAssignmentId());
        assertEquals(10L, history.getTaskId());
        assertEquals(101L, history.getTechnicianId());
        assertEquals(102L, history.getPreviousTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.REASSIGNED, history.getAction());
        assertEquals("dispatcher@fsm.com", history.getActionBy());
        assertEquals(now, history.getActionAt());
        assertEquals("Test reason", history.getReason());
    }
    
    @Test
    void testHistoryNotEqualsWithDifferentAssignmentId() {
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory history1 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        AssignmentHistory history2 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(200L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        assertNotEquals(history1, history2);
    }
    
    @Test
    void testHistoryNotEqualsWithDifferentAction() {
        LocalDateTime now = LocalDateTime.now();
        AssignmentHistory history1 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        AssignmentHistory history2 = AssignmentHistory.builder()
                .id(1L)
                .assignmentId(100L)
                .taskId(10L)
                .technicianId(101L)
                .action(AssignmentHistory.HistoryAction.COMPLETED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(now)
                .build();
        
        assertNotEquals(history1, history2);
    }
}
