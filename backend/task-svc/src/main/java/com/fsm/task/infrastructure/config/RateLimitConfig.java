package com.fsm.task.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for rate limiting address autocomplete API calls.
 * Uses Bucket4j token bucket algorithm for rate limiting.
 */
@Configuration
public class RateLimitConfig {
    
    /**
     * Maximum number of requests allowed per time window.
     */
    private static final int REQUESTS_PER_MINUTE = 60;
    
    /**
     * Creates a rate limiter bucket for address autocomplete requests.
     * Limits to 60 requests per minute to prevent API quota exhaustion.
     * 
     * @return configured Bucket for rate limiting
     */
    @Bean
    public Bucket addressAutocompleteBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REQUESTS_PER_MINUTE)
                .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
