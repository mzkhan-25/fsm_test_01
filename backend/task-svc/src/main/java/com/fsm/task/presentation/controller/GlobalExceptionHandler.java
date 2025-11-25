package com.fsm.task.presentation.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 * Handles validation exceptions and converts them to appropriate HTTP responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handles constraint violation exceptions (e.g., from @Valid on request parameters)
     * 
     * @param ex the constraint violation exception
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        log.debug("Constraint violation: {}", ex.getMessage());
        
        String errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("message", errors);
        
        return ResponseEntity.badRequest().body(body);
    }
    
    /**
     * Handles missing request parameter exceptions
     * 
     * @param ex the missing parameter exception
     * @return ResponseEntity with error details and 400 status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.debug("Missing parameter: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Missing parameter");
        body.put("message", "Required parameter '" + ex.getParameterName() + "' is missing");
        
        return ResponseEntity.badRequest().body(body);
    }
}
