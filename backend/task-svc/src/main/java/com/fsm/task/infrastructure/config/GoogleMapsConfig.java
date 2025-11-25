package com.fsm.task.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Google Maps API integration.
 * The API key should be provided via environment variable GOOGLE_MAPS_API_KEY.
 */
@Configuration
@ConfigurationProperties(prefix = "google.maps")
@Data
public class GoogleMapsConfig {
    
    /**
     * Google Maps API key for Places API access.
     * Should be configured via environment variable for security.
     */
    private String apiKey;
    
    /**
     * Base URL for Google Maps Places API.
     */
    private String placesApiUrl = "https://maps.googleapis.com/maps/api/place";
    
    /**
     * Maximum number of suggestions to return.
     */
    private int maxSuggestions = 5;
    
    /**
     * Checks if the API key is configured.
     * 
     * @return true if API key is present and not blank
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
