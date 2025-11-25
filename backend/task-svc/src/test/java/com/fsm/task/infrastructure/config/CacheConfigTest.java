package com.fsm.task.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CacheConfig
 */
class CacheConfigTest {
    
    @Test
    void testCacheManagerCreation() {
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager();
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof CaffeineCacheManager);
    }
    
    @Test
    void testAddressSuggestionsCacheExists() {
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager();
        
        assertNotNull(cacheManager.getCache(CacheConfig.ADDRESS_SUGGESTIONS_CACHE));
    }
    
    @Test
    void testCacheNameConstant() {
        assertEquals("addressSuggestions", CacheConfig.ADDRESS_SUGGESTIONS_CACHE);
    }
}
