package com.fsm.task.application.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitExceededException
 */
class RateLimitExceededExceptionTest {
    
    @Test
    void testDefaultConstructor() {
        RateLimitExceededException exception = new RateLimitExceededException();
        
        assertEquals("Rate limit exceeded. Please try again later.", exception.getMessage());
    }
    
    @Test
    void testMessageConstructor() {
        String customMessage = "Custom rate limit message";
        RateLimitExceededException exception = new RateLimitExceededException(customMessage);
        
        assertEquals(customMessage, exception.getMessage());
    }
    
    @Test
    void testIsRuntimeException() {
        RateLimitExceededException exception = new RateLimitExceededException();
        
        assertTrue(exception instanceof RuntimeException);
    }
}
