package com.fsm.task.application.service;

import com.fsm.task.application.dto.AddressSuggestionResponse;
import com.fsm.task.application.exception.RateLimitExceededException;
import com.fsm.task.infrastructure.config.CacheConfig;
import com.fsm.task.infrastructure.config.GoogleMapsConfig;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for address autocomplete functionality.
 * Integrates with Google Maps Places API to provide address suggestions.
 * Includes rate limiting and caching for performance and API quota management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressAutocompleteService {
    
    private final GoogleMapsConfig googleMapsConfig;
    private final Bucket addressAutocompleteBucket;
    private final RestTemplate restTemplate;
    
    /**
     * Gets address suggestions for a partial address input.
     * Results are cached to reduce external API calls.
     * Rate limiting is applied to prevent API quota exhaustion.
     * 
     * @param partialAddress the partial address to search for
     * @return list of address suggestions with coordinates
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    @Cacheable(value = CacheConfig.ADDRESS_SUGGESTIONS_CACHE, key = "#partialAddress.toLowerCase()")
    public List<AddressSuggestionResponse> getAddressSuggestions(String partialAddress) {
        log.info("Fetching address suggestions for: {}", partialAddress);
        
        if (!addressAutocompleteBucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for address autocomplete");
            throw new RateLimitExceededException();
        }
        
        if (partialAddress == null || partialAddress.trim().length() < 3) {
            log.debug("Partial address too short, returning empty list");
            return new ArrayList<>();
        }
        
        if (!googleMapsConfig.isConfigured()) {
            log.warn("Google Maps API key not configured, returning empty suggestions");
            return new ArrayList<>();
        }
        
        return fetchSuggestionsFromGoogle(partialAddress);
    }
    
    /**
     * Fetches address suggestions from Google Maps Places API.
     * 
     * @param partialAddress the partial address to search for
     * @return list of address suggestions
     */
    @SuppressWarnings("unchecked")
    private List<AddressSuggestionResponse> fetchSuggestionsFromGoogle(String partialAddress) {
        List<AddressSuggestionResponse> suggestions = new ArrayList<>();
        
        try {
            // First, get autocomplete predictions
            String autocompleteUrl = UriComponentsBuilder
                    .fromHttpUrl(googleMapsConfig.getPlacesApiUrl() + "/autocomplete/json")
                    .queryParam("input", partialAddress)
                    .queryParam("types", "address")
                    .queryParam("key", googleMapsConfig.getApiKey())
                    .build()
                    .toUriString();
            
            ResponseEntity<Map> autocompleteResponse = restTemplate.getForEntity(autocompleteUrl, Map.class);
            
            if (autocompleteResponse.getBody() == null) {
                log.warn("Empty response from Google Places API");
                return suggestions;
            }
            
            List<Map<String, Object>> predictions = (List<Map<String, Object>>) autocompleteResponse.getBody().get("predictions");
            
            if (predictions == null || predictions.isEmpty()) {
                log.debug("No predictions found for: {}", partialAddress);
                return suggestions;
            }
            
            int count = 0;
            for (Map<String, Object> prediction : predictions) {
                if (count >= googleMapsConfig.getMaxSuggestions()) {
                    break;
                }
                
                String placeId = (String) prediction.get("place_id");
                String formattedAddress = (String) prediction.get("description");
                
                // Get place details for coordinates
                AddressSuggestionResponse suggestion = getPlaceDetails(placeId, formattedAddress);
                if (suggestion != null) {
                    suggestions.add(suggestion);
                    count++;
                }
            }
            
        } catch (RestClientException e) {
            log.error("Error calling Google Maps API: {}", e.getMessage());
        }
        
        return suggestions;
    }
    
    /**
     * Gets place details including coordinates from Google Places API.
     * 
     * @param placeId the Google place ID
     * @param formattedAddress the formatted address string
     * @return address suggestion with coordinates, or null if not found
     */
    @SuppressWarnings("unchecked")
    private AddressSuggestionResponse getPlaceDetails(String placeId, String formattedAddress) {
        try {
            String detailsUrl = UriComponentsBuilder
                    .fromHttpUrl(googleMapsConfig.getPlacesApiUrl() + "/details/json")
                    .queryParam("place_id", placeId)
                    .queryParam("fields", "geometry")
                    .queryParam("key", googleMapsConfig.getApiKey())
                    .build()
                    .toUriString();
            
            ResponseEntity<Map> detailsResponse = restTemplate.getForEntity(detailsUrl, Map.class);
            
            if (detailsResponse.getBody() == null) {
                return null;
            }
            
            Map<String, Object> result = (Map<String, Object>) detailsResponse.getBody().get("result");
            if (result == null) {
                return null;
            }
            
            Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
            if (geometry == null) {
                return null;
            }
            
            Map<String, Object> location = (Map<String, Object>) geometry.get("location");
            if (location == null) {
                return null;
            }
            
            Double lat = ((Number) location.get("lat")).doubleValue();
            Double lng = ((Number) location.get("lng")).doubleValue();
            
            return AddressSuggestionResponse.builder()
                    .formattedAddress(formattedAddress)
                    .latitude(lat)
                    .longitude(lng)
                    .placeId(placeId)
                    .build();
            
        } catch (RestClientException e) {
            log.error("Error fetching place details for {}: {}", placeId, e.getMessage());
            return null;
        }
    }
}
