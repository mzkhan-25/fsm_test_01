package com.fsm.task.application.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AddressSuggestionResponse
 */
class AddressSuggestionResponseTest {
    
    @Test
    void testBuilder() {
        AddressSuggestionResponse response = AddressSuggestionResponse.builder()
                .formattedAddress("123 Main St, Springfield, IL")
                .latitude(39.7817)
                .longitude(-89.6501)
                .placeId("ChIJtest123")
                .build();
        
        assertEquals("123 Main St, Springfield, IL", response.getFormattedAddress());
        assertEquals(39.7817, response.getLatitude());
        assertEquals(-89.6501, response.getLongitude());
        assertEquals("ChIJtest123", response.getPlaceId());
    }
    
    @Test
    void testNoArgsConstructor() {
        AddressSuggestionResponse response = new AddressSuggestionResponse();
        
        assertNull(response.getFormattedAddress());
        assertNull(response.getLatitude());
        assertNull(response.getLongitude());
        assertNull(response.getPlaceId());
    }
    
    @Test
    void testAllArgsConstructor() {
        AddressSuggestionResponse response = new AddressSuggestionResponse(
                "456 Oak Ave, Chicago, IL",
                41.8781,
                -87.6298,
                "ChIJtest456"
        );
        
        assertEquals("456 Oak Ave, Chicago, IL", response.getFormattedAddress());
        assertEquals(41.8781, response.getLatitude());
        assertEquals(-87.6298, response.getLongitude());
        assertEquals("ChIJtest456", response.getPlaceId());
    }
    
    @Test
    void testSettersAndGetters() {
        AddressSuggestionResponse response = new AddressSuggestionResponse();
        
        response.setFormattedAddress("789 Pine Rd, Denver, CO");
        response.setLatitude(39.7392);
        response.setLongitude(-104.9903);
        response.setPlaceId("ChIJtest789");
        
        assertEquals("789 Pine Rd, Denver, CO", response.getFormattedAddress());
        assertEquals(39.7392, response.getLatitude());
        assertEquals(-104.9903, response.getLongitude());
        assertEquals("ChIJtest789", response.getPlaceId());
    }
    
    @Test
    void testEquals() {
        AddressSuggestionResponse response1 = AddressSuggestionResponse.builder()
                .formattedAddress("123 Main St")
                .latitude(39.7817)
                .longitude(-89.6501)
                .placeId("test123")
                .build();
        
        AddressSuggestionResponse response2 = AddressSuggestionResponse.builder()
                .formattedAddress("123 Main St")
                .latitude(39.7817)
                .longitude(-89.6501)
                .placeId("test123")
                .build();
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    void testNotEquals() {
        AddressSuggestionResponse response1 = AddressSuggestionResponse.builder()
                .formattedAddress("123 Main St")
                .placeId("test123")
                .build();
        
        AddressSuggestionResponse response2 = AddressSuggestionResponse.builder()
                .formattedAddress("456 Oak Ave")
                .placeId("test456")
                .build();
        
        assertNotEquals(response1, response2);
    }
    
    @Test
    void testToString() {
        AddressSuggestionResponse response = AddressSuggestionResponse.builder()
                .formattedAddress("123 Main St")
                .latitude(39.7817)
                .longitude(-89.6501)
                .placeId("test123")
                .build();
        
        String toString = response.toString();
        assertTrue(toString.contains("123 Main St"));
        assertTrue(toString.contains("39.7817"));
    }
}
