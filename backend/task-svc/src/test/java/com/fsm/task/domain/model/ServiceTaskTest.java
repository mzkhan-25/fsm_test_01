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
 * Unit tests for ServiceTask entity
 */
class ServiceTaskTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testServiceTaskBuilderCreatesValidTask() {
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .estimatedDuration(60)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .createdBy("test@example.com")
                .build();
        
        assertNotNull(task);
        assertEquals(1L, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals("123 Test St", task.getClientAddress());
        assertEquals(ServiceTask.Priority.HIGH, task.getPriority());
        assertEquals(60, task.getEstimatedDuration());
        assertEquals(ServiceTask.TaskStatus.UNASSIGNED, task.getStatus());
        assertEquals("test@example.com", task.getCreatedBy());
    }
    
    @Test
    void testServiceTaskDefaultStatus() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.MEDIUM)
                .build();
        
        assertEquals(ServiceTask.TaskStatus.UNASSIGNED, task.getStatus(), 
                "Default status should be UNASSIGNED");
    }
    
    @Test
    void testValidationWithBlankTitle() {
        ServiceTask task = ServiceTask.builder()
                .title("")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testValidationWithNullTitle() {
        ServiceTask task = ServiceTask.builder()
                .title(null)
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testValidationWithShortTitle() {
        ServiceTask task = ServiceTask.builder()
                .title("ab")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testValidationWithMinimumValidTitle() {
        ServiceTask task = ServiceTask.builder()
                .title("abc")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testValidationWithBlankClientAddress() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("clientAddress")));
    }
    
    @Test
    void testValidationWithNullClientAddress() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress(null)
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("clientAddress")));
    }
    
    @Test
    void testValidationWithNullPriority() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(null)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("priority")));
    }
    
    @Test
    void testValidationWithNullStatus() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(null)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }
    
    @Test
    void testValidationWithNegativeEstimatedDuration() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .estimatedDuration(-10)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("estimatedDuration")));
    }
    
    @Test
    void testValidationWithZeroEstimatedDuration() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .estimatedDuration(0)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("estimatedDuration")));
    }
    
    @Test
    void testValidationWithPositiveEstimatedDuration() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .estimatedDuration(60)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("estimatedDuration")));
    }
    
    @Test
    void testValidationWithNullEstimatedDuration() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .estimatedDuration(null)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        // Estimated duration is optional, so null should be valid
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("estimatedDuration")));
    }
    
    @Test
    void testPriorityEnum() {
        assertEquals(3, ServiceTask.Priority.values().length, 
                "Should have exactly 3 priority values");
        assertNotNull(ServiceTask.Priority.HIGH);
        assertNotNull(ServiceTask.Priority.MEDIUM);
        assertNotNull(ServiceTask.Priority.LOW);
    }
    
    @Test
    void testTaskStatusEnum() {
        assertEquals(4, ServiceTask.TaskStatus.values().length, 
                "Should have exactly 4 status values");
        assertNotNull(ServiceTask.TaskStatus.UNASSIGNED);
        assertNotNull(ServiceTask.TaskStatus.ASSIGNED);
        assertNotNull(ServiceTask.TaskStatus.IN_PROGRESS);
        assertNotNull(ServiceTask.TaskStatus.COMPLETED);
    }
    
    @Test
    void testAssignMethod() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        assertTrue(task.isUnassigned());
        task.assign();
        assertEquals(ServiceTask.TaskStatus.ASSIGNED, task.getStatus());
    }
    
    @Test
    void testAssignMethodOnlyFromUnassigned() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.COMPLETED)
                .build();
        
        task.assign();
        // Should not change status if not unassigned
        assertEquals(ServiceTask.TaskStatus.COMPLETED, task.getStatus());
    }
    
    @Test
    void testStartMethod() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.ASSIGNED)
                .build();
        
        task.start();
        assertEquals(ServiceTask.TaskStatus.IN_PROGRESS, task.getStatus());
    }
    
    @Test
    void testStartMethodOnlyFromAssigned() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        task.start();
        // Should not change status if not assigned
        assertEquals(ServiceTask.TaskStatus.UNASSIGNED, task.getStatus());
    }
    
    @Test
    void testCompleteMethod() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.IN_PROGRESS)
                .build();
        
        assertFalse(task.isCompleted());
        task.complete();
        assertTrue(task.isCompleted());
        assertEquals(ServiceTask.TaskStatus.COMPLETED, task.getStatus());
    }
    
    @Test
    void testCompleteMethodOnlyFromInProgress() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.ASSIGNED)
                .build();
        
        task.complete();
        // Should not change status if not in progress
        assertEquals(ServiceTask.TaskStatus.ASSIGNED, task.getStatus());
    }
    
    @Test
    void testIsCompletedMethod() {
        ServiceTask completedTask = ServiceTask.builder()
                .title("Completed Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.COMPLETED)
                .build();
        
        ServiceTask uncompletedTask = ServiceTask.builder()
                .title("Uncompleted Task")
                .clientAddress("456 Test Ave")
                .priority(ServiceTask.Priority.LOW)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        assertTrue(completedTask.isCompleted());
        assertFalse(uncompletedTask.isCompleted());
    }
    
    @Test
    void testIsUnassignedMethod() {
        ServiceTask unassignedTask = ServiceTask.builder()
                .title("Unassigned Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask assignedTask = ServiceTask.builder()
                .title("Assigned Task")
                .clientAddress("456 Test Ave")
                .priority(ServiceTask.Priority.LOW)
                .status(ServiceTask.TaskStatus.ASSIGNED)
                .build();
        
        assertTrue(unassignedTask.isUnassigned());
        assertFalse(assignedTask.isUnassigned());
    }
    
    @Test
    void testTaskLifecycleTransitions() {
        ServiceTask task = ServiceTask.builder()
                .title("Lifecycle Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.MEDIUM)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        // Unassigned -> Assigned
        assertTrue(task.isUnassigned());
        task.assign();
        assertEquals(ServiceTask.TaskStatus.ASSIGNED, task.getStatus());
        
        // Assigned -> In Progress
        task.start();
        assertEquals(ServiceTask.TaskStatus.IN_PROGRESS, task.getStatus());
        
        // In Progress -> Completed
        task.complete();
        assertTrue(task.isCompleted());
        assertEquals(ServiceTask.TaskStatus.COMPLETED, task.getStatus());
    }
    
    @Test
    void testServiceTaskEqualsAndHashCode() {
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }
    
    @Test
    void testServiceTaskToString() {
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        String toString = task.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Task"));
    }
    
    @Test
    void testServiceTaskEqualsWithNull() {
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        assertNotEquals(task, null);
    }
    
    @Test
    void testServiceTaskEqualsWithDifferentClass() {
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        assertNotEquals(task, "not a task");
    }
    
    @Test
    void testServiceTaskEqualsSameObject() {
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        assertEquals(task, task);
    }
    
    @Test
    void testServiceTaskAllArgsConstructor() {
        ServiceTask task = new ServiceTask(1L, "Test", "Description", 
                "123 Test St", ServiceTask.Priority.HIGH, 60, 
                ServiceTask.TaskStatus.UNASSIGNED, "test@example.com", 
                LocalDateTime.now(), null);
        
        assertNotNull(task);
        assertEquals(1L, task.getId());
        assertEquals("Test", task.getTitle());
    }
    
    @Test
    void testServiceTaskSetTitle() {
        ServiceTask task = new ServiceTask();
        task.setTitle("New Title");
        
        assertEquals("New Title", task.getTitle());
    }
    
    @Test
    void testServiceTaskSetDescription() {
        ServiceTask task = new ServiceTask();
        task.setDescription("New Description");
        
        assertEquals("New Description", task.getDescription());
    }
    
    @Test
    void testServiceTaskSetClientAddress() {
        ServiceTask task = new ServiceTask();
        task.setClientAddress("456 New St");
        
        assertEquals("456 New St", task.getClientAddress());
    }
    
    @Test
    void testServiceTaskSetPriority() {
        ServiceTask task = new ServiceTask();
        task.setPriority(ServiceTask.Priority.LOW);
        
        assertEquals(ServiceTask.Priority.LOW, task.getPriority());
    }
    
    @Test
    void testServiceTaskSetEstimatedDuration() {
        ServiceTask task = new ServiceTask();
        task.setEstimatedDuration(90);
        
        assertEquals(90, task.getEstimatedDuration());
    }
    
    @Test
    void testServiceTaskSetStatus() {
        ServiceTask task = new ServiceTask();
        task.setStatus(ServiceTask.TaskStatus.COMPLETED);
        
        assertEquals(ServiceTask.TaskStatus.COMPLETED, task.getStatus());
    }
    
    @Test
    void testServiceTaskSetCreatedBy() {
        ServiceTask task = new ServiceTask();
        task.setCreatedBy("admin@example.com");
        
        assertEquals("admin@example.com", task.getCreatedBy());
    }
    
    @Test
    void testServiceTaskSetId() {
        ServiceTask task = new ServiceTask();
        task.setId(99L);
        
        assertEquals(99L, task.getId());
    }
    
    @Test
    void testServiceTaskSetCreatedAt() {
        ServiceTask task = new ServiceTask();
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        
        assertEquals(now, task.getCreatedAt());
    }
    
    @Test
    void testServiceTaskOnCreate() {
        ServiceTask task = new ServiceTask();
        assertNull(task.getCreatedAt());
        
        task.onCreate();
        
        assertNotNull(task.getCreatedAt());
    }
    
    @Test
    void testServiceTaskBuilder() {
        ServiceTask task = ServiceTask.builder().build();
        
        assertNotNull(task);
    }
    
    @Test
    void testServiceTaskCanEqual() {
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Test")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(1L)
                .title("Test")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        assertTrue(task1.canEqual(task2));
        assertTrue(task2.canEqual(task1));
    }
    
    @Test
    void testServiceTaskBuilderWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        ServiceTask task = ServiceTask.builder()
                .id(10L)
                .title("Complete Task")
                .description("Full description")
                .clientAddress("Complete Address")
                .priority(ServiceTask.Priority.MEDIUM)
                .estimatedDuration(120)
                .status(ServiceTask.TaskStatus.IN_PROGRESS)
                .createdBy("admin@example.com")
                .createdAt(now)
                .build();
        
        assertEquals(10L, task.getId());
        assertEquals("Complete Task", task.getTitle());
        assertEquals("Full description", task.getDescription());
        assertEquals("Complete Address", task.getClientAddress());
        assertEquals(ServiceTask.Priority.MEDIUM, task.getPriority());
        assertEquals(120, task.getEstimatedDuration());
        assertEquals(ServiceTask.TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals("admin@example.com", task.getCreatedBy());
        assertEquals(now, task.getCreatedAt());
    }
    
    @Test
    void testServiceTaskBuilderToString() {
        ServiceTask.ServiceTaskBuilder builder = ServiceTask.builder()
                .title("Builder Test")
                .clientAddress("123 Test St");
        
        String builderStr = builder.toString();
        assertNotNull(builderStr);
        assertTrue(builderStr.contains("ServiceTask") || builderStr.contains("ServiceTaskBuilder"));
    }
    
    @Test
    void testServiceTaskNotEqualsWithDifferentId() {
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Test")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(2L)
                .title("Test")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        assertNotEquals(task1, task2);
    }
    
    @Test
    void testServiceTaskNotEqualsWithDifferentTitle() {
        ServiceTask task1 = ServiceTask.builder()
                .id(1L)
                .title("Test 1")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(1L)
                .title("Test 2")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .build();
        
        assertNotEquals(task1, task2);
    }
    
    @Test
    void testValidationWithValidTask() {
        ServiceTask task = ServiceTask.builder()
                .title("Valid Task")
                .description("This is a valid task")
                .clientAddress("123 Main St, City, State 12345")
                .priority(ServiceTask.Priority.HIGH)
                .estimatedDuration(120)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .createdBy("test@example.com")
                .build();
        
        Set<ConstraintViolation<ServiceTask>> violations = validator.validate(task);
        assertTrue(violations.isEmpty(), "Valid task should have no violations");
    }
    
    @Test
    void testTaskWithAllPriorities() {
        ServiceTask.Priority[] priorities = {
            ServiceTask.Priority.HIGH, 
            ServiceTask.Priority.MEDIUM, 
            ServiceTask.Priority.LOW
        };
        
        for (ServiceTask.Priority priority : priorities) {
            ServiceTask task = ServiceTask.builder()
                    .title("Test Task")
                    .clientAddress("123 Test St")
                    .priority(priority)
                    .status(ServiceTask.TaskStatus.UNASSIGNED)
                    .build();
            
            assertEquals(priority, task.getPriority());
        }
    }
    
    @Test
    void testTaskWithAllStatuses() {
        ServiceTask.TaskStatus[] statuses = {
            ServiceTask.TaskStatus.UNASSIGNED,
            ServiceTask.TaskStatus.ASSIGNED,
            ServiceTask.TaskStatus.IN_PROGRESS,
            ServiceTask.TaskStatus.COMPLETED
        };
        
        for (ServiceTask.TaskStatus status : statuses) {
            ServiceTask task = ServiceTask.builder()
                    .title("Test Task")
                    .clientAddress("123 Test St")
                    .priority(ServiceTask.Priority.MEDIUM)
                    .status(status)
                    .build();
            
            assertEquals(status, task.getStatus());
        }
    }
    
    @Test
    void testAssignToTechnicianMethod() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        assertTrue(task.isUnassigned());
        assertNull(task.getAssignedTechnicianId());
        
        task.assignToTechnician(101L);
        
        assertEquals(ServiceTask.TaskStatus.ASSIGNED, task.getStatus());
        assertEquals(101L, task.getAssignedTechnicianId());
    }
    
    @Test
    void testAssignToTechnicianOnlyFromUnassigned() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.COMPLETED)
                .build();
        
        task.assignToTechnician(101L);
        
        // Should not change status if not unassigned
        assertEquals(ServiceTask.TaskStatus.COMPLETED, task.getStatus());
        assertNull(task.getAssignedTechnicianId());
    }
    
    @Test
    void testReassignToTechnicianMethod() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.ASSIGNED)
                .assignedTechnicianId(101L)
                .build();
        
        task.reassignToTechnician(102L);
        
        assertEquals(ServiceTask.TaskStatus.ASSIGNED, task.getStatus());
        assertEquals(102L, task.getAssignedTechnicianId());
    }
    
    @Test
    void testReassignToTechnicianOnlyFromAssigned() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        task.reassignToTechnician(101L);
        
        // Should not change if not assigned
        assertEquals(ServiceTask.TaskStatus.UNASSIGNED, task.getStatus());
        assertNull(task.getAssignedTechnicianId());
    }
    
    @Test
    void testCanBeAssignedFromUnassigned() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        assertTrue(task.canBeAssigned());
    }
    
    @Test
    void testCanBeAssignedFromAssigned() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.ASSIGNED)
                .build();
        
        assertTrue(task.canBeAssigned());
    }
    
    @Test
    void testCannotBeAssignedFromInProgress() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.IN_PROGRESS)
                .build();
        
        assertFalse(task.canBeAssigned());
    }
    
    @Test
    void testCannotBeAssignedFromCompleted() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.COMPLETED)
                .build();
        
        assertFalse(task.canBeAssigned());
    }
    
    @Test
    void testIsAssignedMethod() {
        ServiceTask assignedTask = ServiceTask.builder()
                .title("Assigned Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.ASSIGNED)
                .build();
        
        ServiceTask unassignedTask = ServiceTask.builder()
                .title("Unassigned Task")
                .clientAddress("456 Test Ave")
                .priority(ServiceTask.Priority.LOW)
                .status(ServiceTask.TaskStatus.UNASSIGNED)
                .build();
        
        assertTrue(assignedTask.isAssigned());
        assertFalse(unassignedTask.isAssigned());
    }
    
    @Test
    void testAssignedTechnicianIdField() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(ServiceTask.Priority.HIGH)
                .status(ServiceTask.TaskStatus.ASSIGNED)
                .assignedTechnicianId(101L)
                .build();
        
        assertEquals(101L, task.getAssignedTechnicianId());
    }
    
    @Test
    void testSetAssignedTechnicianId() {
        ServiceTask task = new ServiceTask();
        task.setAssignedTechnicianId(101L);
        
        assertEquals(101L, task.getAssignedTechnicianId());
    }
}
