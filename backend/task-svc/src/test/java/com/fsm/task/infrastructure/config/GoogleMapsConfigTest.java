package com.fsm.task.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GoogleMapsConfig
 */
class GoogleMapsConfigTest {
    
    @Test
    void testIsConfigured_WhenApiKeyIsSet() {
        GoogleMapsConfig config = new GoogleMapsConfig();
        config.setApiKey("test-api-key");
        
        assertTrue(config.isConfigured());
    }
    
    @Test
    void testIsConfigured_WhenApiKeyIsNull() {
        GoogleMapsConfig config = new GoogleMapsConfig();
        config.setApiKey(null);
        
        assertFalse(config.isConfigured());
    }
    
    @Test
    void testIsConfigured_WhenApiKeyIsEmpty() {
        GoogleMapsConfig config = new GoogleMapsConfig();
        config.setApiKey("");
        
        assertFalse(config.isConfigured());
    }
    
    @Test
    void testIsConfigured_WhenApiKeyIsBlank() {
        GoogleMapsConfig config = new GoogleMapsConfig();
        config.setApiKey("   ");
        
        assertFalse(config.isConfigured());
    }
    
    @Test
    void testDefaultValues() {
        GoogleMapsConfig config = new GoogleMapsConfig();
        
        assertEquals("https://maps.googleapis.com/maps/api/place", config.getPlacesApiUrl());
        assertEquals(5, config.getMaxSuggestions());
    }
    
    @Test
    void testSettersAndGetters() {
        GoogleMapsConfig config = new GoogleMapsConfig();
        
        config.setApiKey("my-key");
        config.setPlacesApiUrl("https://custom.url.com");
        config.setMaxSuggestions(10);
        
        assertEquals("my-key", config.getApiKey());
        assertEquals("https://custom.url.com", config.getPlacesApiUrl());
        assertEquals(10, config.getMaxSuggestions());
    }
}
