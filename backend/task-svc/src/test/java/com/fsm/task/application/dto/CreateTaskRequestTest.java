package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask.Priority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateTaskRequest DTO validation
 */
class CreateTaskRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidCreateTaskRequest() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task Title")
                .description("Test description")
                .clientAddress("123 Main St, City, State 12345")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }
    
    @Test
    void testValidRequestWithoutOptionalFields() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.MEDIUM)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Request without optional fields should be valid");
    }
    
    @Test
    void testBlankTitleValidation() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testNullTitleValidation() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title(null)
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testShortTitleValidation() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("ab")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testMinimumValidTitleLength() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("abc")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testMaximumTitleLength() {
        String longTitle = "a".repeat(201);
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title(longTitle)
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testValidMaxTitleLength() {
        String maxTitle = "a".repeat(200);
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title(maxTitle)
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testBlankClientAddressValidation() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("")
                .priority(Priority.HIGH)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("clientAddress")));
    }
    
    @Test
    void testNullClientAddressValidation() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress(null)
                .priority(Priority.HIGH)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("clientAddress")));
    }
    
    @Test
    void testNullPriorityValidation() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(null)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("priority")));
    }
    
    @Test
    void testNegativeEstimatedDurationValidation() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(-10)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("estimatedDuration")));
    }
    
    @Test
    void testZeroEstimatedDurationValidation() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(0)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("estimatedDuration")));
    }
    
    @Test
    void testValidPositiveEstimatedDuration() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("estimatedDuration")));
    }
    
    @Test
    void testNullEstimatedDurationIsValid() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(null)
                .build();
        
        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("estimatedDuration")));
    }
    
    @Test
    void testAllPriorityValues() {
        for (Priority priority : Priority.values()) {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .clientAddress("123 Main St")
                    .priority(priority)
                    .build();
            
            Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Priority " + priority + " should be valid");
        }
    }
    
    @Test
    void testBuilderPattern() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Builder Test")
                .description("Testing builder pattern")
                .clientAddress("456 Test Ave")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .build();
        
        assertNotNull(request);
        assertEquals("Builder Test", request.getTitle());
        assertEquals("Testing builder pattern", request.getDescription());
        assertEquals("456 Test Ave", request.getClientAddress());
        assertEquals(Priority.MEDIUM, request.getPriority());
        assertEquals(90, request.getEstimatedDuration());
    }
    
    @Test
    void testNoArgsConstructor() {
        CreateTaskRequest request = new CreateTaskRequest();
        assertNotNull(request);
    }
    
    @Test
    void testAllArgsConstructor() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Title", "Description", "Address", Priority.LOW, 60);
        
        assertEquals("Title", request.getTitle());
        assertEquals("Description", request.getDescription());
        assertEquals("Address", request.getClientAddress());
        assertEquals(Priority.LOW, request.getPriority());
        assertEquals(60, request.getEstimatedDuration());
    }
    
    @Test
    void testSettersAndGetters() {
        CreateTaskRequest request = new CreateTaskRequest();
        
        request.setTitle("Test Title");
        request.setDescription("Test Description");
        request.setClientAddress("Test Address");
        request.setPriority(Priority.HIGH);
        request.setEstimatedDuration(120);
        
        assertEquals("Test Title", request.getTitle());
        assertEquals("Test Description", request.getDescription());
        assertEquals("Test Address", request.getClientAddress());
        assertEquals(Priority.HIGH, request.getPriority());
        assertEquals(120, request.getEstimatedDuration());
    }
    
    @Test
    void testEqualsAndHashCode() {
        CreateTaskRequest request1 = CreateTaskRequest.builder()
                .title("Test")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();
        
        CreateTaskRequest request2 = CreateTaskRequest.builder()
                .title("Test")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testToString() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .build();
        
        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test"));
    }
}
