package com.fsm.task.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CompleteTaskRequest DTO
 */
class CompleteTaskRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidRequest() {
        CompleteTaskRequest request = CompleteTaskRequest.builder()
                .workSummary("Replaced HVAC compressor and tested")
                .build();
        
        Set<ConstraintViolation<CompleteTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testWorkSummaryRequired() {
        CompleteTaskRequest request = CompleteTaskRequest.builder()
                .workSummary(null)
                .build();
        
        Set<ConstraintViolation<CompleteTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Work summary is required")));
    }
    
    @Test
    void testWorkSummaryBlank() {
        CompleteTaskRequest request = CompleteTaskRequest.builder()
                .workSummary("   ")
                .build();
        
        Set<ConstraintViolation<CompleteTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testWorkSummaryMinimumLength() {
        CompleteTaskRequest request = CompleteTaskRequest.builder()
                .workSummary("Short")
                .build();
        
        Set<ConstraintViolation<CompleteTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("at least 10 characters")));
    }
    
    @Test
    void testWorkSummaryExactlyMinimumLength() {
        CompleteTaskRequest request = CompleteTaskRequest.builder()
                .workSummary("1234567890") // Exactly 10 characters
                .build();
        
        Set<ConstraintViolation<CompleteTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testWorkSummaryLongText() {
        String longSummary = "This is a very detailed work summary that describes all the steps " +
                "taken to complete the task, including diagnostics, repairs, testing, and " +
                "validation of the system functionality.";
        
        CompleteTaskRequest request = CompleteTaskRequest.builder()
                .workSummary(longSummary)
                .build();
        
        Set<ConstraintViolation<CompleteTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testGettersAndSetters() {
        CompleteTaskRequest request = new CompleteTaskRequest();
        String workSummary = "Test work summary";
        
        request.setWorkSummary(workSummary);
        
        assertEquals(workSummary, request.getWorkSummary());
    }
    
    @Test
    void testBuilder() {
        String workSummary = "Work completed successfully";
        
        CompleteTaskRequest request = CompleteTaskRequest.builder()
                .workSummary(workSummary)
                .build();
        
        assertNotNull(request);
        assertEquals(workSummary, request.getWorkSummary());
    }
    
    @Test
    void testNoArgsConstructor() {
        CompleteTaskRequest request = new CompleteTaskRequest();
        assertNotNull(request);
    }
    
    @Test
    void testAllArgsConstructor() {
        String workSummary = "Work completed successfully";
        CompleteTaskRequest request = new CompleteTaskRequest(workSummary);
        
        assertNotNull(request);
        assertEquals(workSummary, request.getWorkSummary());
    }
}
