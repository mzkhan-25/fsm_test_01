package com.fsm.task.application.service;

import com.fsm.task.application.dto.AddressSuggestionResponse;
import com.fsm.task.application.exception.RateLimitExceededException;
import com.fsm.task.infrastructure.config.GoogleMapsConfig;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressAutocompleteService
 */
@ExtendWith(MockitoExtension.class)
class AddressAutocompleteServiceTest {
    
    @Mock
    private GoogleMapsConfig googleMapsConfig;
    
    @Mock
    private Bucket addressAutocompleteBucket;
    
    @Mock
    private RestTemplate restTemplate;
    
    private AddressAutocompleteService addressAutocompleteService;
    
    @BeforeEach
    void setUp() {
        addressAutocompleteService = new AddressAutocompleteService(
                googleMapsConfig, addressAutocompleteBucket, restTemplate);
    }
    
    @Test
    void testGetAddressSuggestions_Success() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        when(googleMapsConfig.getMaxSuggestions()).thenReturn(5);
        
        // Mock autocomplete response
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("place_id", "ChIJtest123");
        prediction.put("description", "123 Main Street, Springfield, IL");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", List.of(prediction));
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        
        // Mock place details response
        Map<String, Object> location = new HashMap<>();
        location.put("lat", 39.7817);
        location.put("lng", -89.6501);
        
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("location", location);
        
        Map<String, Object> result = new HashMap<>();
        result.put("geometry", geometry);
        
        Map<String, Object> detailsBody = new HashMap<>();
        detailsBody.put("result", result);
        
        ResponseEntity<Map> detailsResponse = new ResponseEntity<>(detailsBody, HttpStatus.OK);
        
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        when(restTemplate.getForEntity(contains("details"), eq(Map.class)))
                .thenReturn(detailsResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertEquals(1, suggestions.size());
        assertEquals("123 Main Street, Springfield, IL", suggestions.get(0).getFormattedAddress());
        assertEquals(39.7817, suggestions.get(0).getLatitude());
        assertEquals(-89.6501, suggestions.get(0).getLongitude());
        assertEquals("ChIJtest123", suggestions.get(0).getPlaceId());
    }
    
    @Test
    void testGetAddressSuggestions_RateLimitExceeded() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(false);
        
        // When/Then
        assertThrows(RateLimitExceededException.class, () -> 
                addressAutocompleteService.getAddressSuggestions(partialAddress));
    }
    
    @Test
    void testGetAddressSuggestions_NullInput() {
        // Given
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(null);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_ShortInput() {
        // Given
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions("ab");
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_ApiNotConfigured() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(false);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
        verify(restTemplate, never()).getForEntity(anyString(), any());
    }
    
    @Test
    void testGetAddressSuggestions_EmptyPredictions() {
        // Given
        String partialAddress = "xyz123nonexistent";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", List.of());
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_NullAutocompleteBody() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_NullPredictions() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", null);
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_ApiException() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenThrow(new RestClientException("API error"));
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_PlaceDetailsApiException() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        when(googleMapsConfig.getMaxSuggestions()).thenReturn(5);
        
        // Mock autocomplete response
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("place_id", "ChIJtest123");
        prediction.put("description", "123 Main Street, Springfield, IL");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", List.of(prediction));
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        when(restTemplate.getForEntity(contains("details"), eq(Map.class)))
                .thenThrow(new RestClientException("Details API error"));
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_NullDetailsBody() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        when(googleMapsConfig.getMaxSuggestions()).thenReturn(5);
        
        // Mock autocomplete response
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("place_id", "ChIJtest123");
        prediction.put("description", "123 Main Street, Springfield, IL");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", List.of(prediction));
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        ResponseEntity<Map> detailsResponse = new ResponseEntity<>(null, HttpStatus.OK);
        
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        when(restTemplate.getForEntity(contains("details"), eq(Map.class)))
                .thenReturn(detailsResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_NullResult() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        when(googleMapsConfig.getMaxSuggestions()).thenReturn(5);
        
        // Mock autocomplete response
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("place_id", "ChIJtest123");
        prediction.put("description", "123 Main Street, Springfield, IL");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", List.of(prediction));
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        
        Map<String, Object> detailsBody = new HashMap<>();
        detailsBody.put("result", null);
        
        ResponseEntity<Map> detailsResponse = new ResponseEntity<>(detailsBody, HttpStatus.OK);
        
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        when(restTemplate.getForEntity(contains("details"), eq(Map.class)))
                .thenReturn(detailsResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_NullGeometry() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        when(googleMapsConfig.getMaxSuggestions()).thenReturn(5);
        
        // Mock autocomplete response
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("place_id", "ChIJtest123");
        prediction.put("description", "123 Main Street, Springfield, IL");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", List.of(prediction));
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        
        Map<String, Object> result = new HashMap<>();
        result.put("geometry", null);
        
        Map<String, Object> detailsBody = new HashMap<>();
        detailsBody.put("result", result);
        
        ResponseEntity<Map> detailsResponse = new ResponseEntity<>(detailsBody, HttpStatus.OK);
        
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        when(restTemplate.getForEntity(contains("details"), eq(Map.class)))
                .thenReturn(detailsResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_NullLocation() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        when(googleMapsConfig.getMaxSuggestions()).thenReturn(5);
        
        // Mock autocomplete response
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("place_id", "ChIJtest123");
        prediction.put("description", "123 Main Street, Springfield, IL");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", List.of(prediction));
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("location", null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("geometry", geometry);
        
        Map<String, Object> detailsBody = new HashMap<>();
        detailsBody.put("result", result);
        
        ResponseEntity<Map> detailsResponse = new ResponseEntity<>(detailsBody, HttpStatus.OK);
        
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        when(restTemplate.getForEntity(contains("details"), eq(Map.class)))
                .thenReturn(detailsResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_MaxSuggestionsLimit() {
        // Given
        String partialAddress = "123 Main";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        when(googleMapsConfig.getMaxSuggestions()).thenReturn(2);
        
        // Mock autocomplete response with 5 predictions
        List<Map<String, Object>> predictions = List.of(
                createPrediction("place1", "Address 1"),
                createPrediction("place2", "Address 2"),
                createPrediction("place3", "Address 3"),
                createPrediction("place4", "Address 4"),
                createPrediction("place5", "Address 5")
        );
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", predictions);
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        ResponseEntity<Map> detailsResponse = new ResponseEntity<>(createDetailsBody(40.0, -80.0), HttpStatus.OK);
        
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        when(restTemplate.getForEntity(contains("details"), eq(Map.class)))
                .thenReturn(detailsResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertEquals(2, suggestions.size());
    }
    
    @Test
    void testGetAddressSuggestions_EmptyStringInput() {
        // Given
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions("  ");
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
    
    @Test
    void testGetAddressSuggestions_ExactlyThreeCharacters() {
        // Given
        String partialAddress = "abc";
        when(addressAutocompleteBucket.tryConsume(1)).thenReturn(true);
        when(googleMapsConfig.isConfigured()).thenReturn(true);
        when(googleMapsConfig.getPlacesApiUrl()).thenReturn("https://maps.googleapis.com/maps/api/place");
        when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
        
        Map<String, Object> autocompleteBody = new HashMap<>();
        autocompleteBody.put("predictions", List.of());
        
        ResponseEntity<Map> autocompleteResponse = new ResponseEntity<>(autocompleteBody, HttpStatus.OK);
        when(restTemplate.getForEntity(contains("autocomplete"), eq(Map.class)))
                .thenReturn(autocompleteResponse);
        
        // When
        List<AddressSuggestionResponse> suggestions = 
                addressAutocompleteService.getAddressSuggestions(partialAddress);
        
        // Then
        assertNotNull(suggestions);
        verify(restTemplate).getForEntity(contains("autocomplete"), eq(Map.class));
    }
    
    private Map<String, Object> createPrediction(String placeId, String description) {
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("place_id", placeId);
        prediction.put("description", description);
        return prediction;
    }
    
    private Map<String, Object> createDetailsBody(Double lat, Double lng) {
        Map<String, Object> location = new HashMap<>();
        location.put("lat", lat);
        location.put("lng", lng);
        
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("location", location);
        
        Map<String, Object> result = new HashMap<>();
        result.put("geometry", geometry);
        
        Map<String, Object> detailsBody = new HashMap<>();
        detailsBody.put("result", result);
        return detailsBody;
    }
}
