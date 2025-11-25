package com.fsm.task.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching address autocomplete results.
 * Uses Caffeine cache for high performance in-memory caching.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Cache name for address suggestions.
     */
    public static final String ADDRESS_SUGGESTIONS_CACHE = "addressSuggestions";
    
    /**
     * Configures Caffeine cache manager for address suggestions.
     * Cache entries expire after 1 hour and max 1000 entries.
     * 
     * @return configured CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(ADDRESS_SUGGESTIONS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats());
        return cacheManager;
    }
}
