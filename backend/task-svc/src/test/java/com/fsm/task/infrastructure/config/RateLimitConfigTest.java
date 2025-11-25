package com.fsm.task.infrastructure.config;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitConfig
 */
class RateLimitConfigTest {
    
    @Test
    void testBucketCreation() {
        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        Bucket bucket = rateLimitConfig.addressAutocompleteBucket();
        
        assertNotNull(bucket);
    }
    
    @Test
    void testBucketAllowsRequestsWithinLimit() {
        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        Bucket bucket = rateLimitConfig.addressAutocompleteBucket();
        
        // Should be able to consume 60 tokens (requests per minute)
        for (int i = 0; i < 60; i++) {
            assertTrue(bucket.tryConsume(1), "Should be able to consume token " + (i + 1));
        }
    }
    
    @Test
    void testBucketBlocksRequestsOverLimit() {
        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        Bucket bucket = rateLimitConfig.addressAutocompleteBucket();
        
        // Consume all 60 tokens
        for (int i = 0; i < 60; i++) {
            bucket.tryConsume(1);
        }
        
        // The 61st request should be blocked
        assertFalse(bucket.tryConsume(1), "Should block request over limit");
    }
    
    @Test
    void testBucketInitialCapacity() {
        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        Bucket bucket = rateLimitConfig.addressAutocompleteBucket();
        
        // Should have 60 tokens initially
        assertTrue(bucket.getAvailableTokens() >= 60);
    }
}
