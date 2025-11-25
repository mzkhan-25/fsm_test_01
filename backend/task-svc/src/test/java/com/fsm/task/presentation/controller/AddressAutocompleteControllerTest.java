package com.fsm.task.presentation.controller;

import com.fsm.task.application.dto.AddressSuggestionResponse;
import com.fsm.task.application.exception.RateLimitExceededException;
import com.fsm.task.application.service.AddressAutocompleteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AddressAutocompleteController
 */
@WebMvcTest(AddressAutocompleteController.class)
class AddressAutocompleteControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AddressAutocompleteService addressAutocompleteService;
    
    private List<AddressSuggestionResponse> sampleSuggestions;
    
    @BeforeEach
    void setUp() {
        sampleSuggestions = List.of(
                AddressSuggestionResponse.builder()
                        .formattedAddress("123 Main Street, Springfield, IL 62701, USA")
                        .latitude(39.7817)
                        .longitude(-89.6501)
                        .placeId("ChIJtest123")
                        .build(),
                AddressSuggestionResponse.builder()
                        .formattedAddress("123 Main Avenue, Chicago, IL 60601, USA")
                        .latitude(41.8781)
                        .longitude(-87.6298)
                        .placeId("ChIJtest456")
                        .build()
        );
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_Success() throws Exception {
        when(addressAutocompleteService.getAddressSuggestions("123 Main"))
                .thenReturn(sampleSuggestions);
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "123 Main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].formattedAddress").value("123 Main Street, Springfield, IL 62701, USA"))
                .andExpect(jsonPath("$[0].latitude").value(39.7817))
                .andExpect(jsonPath("$[0].longitude").value(-89.6501))
                .andExpect(jsonPath("$[0].placeId").value("ChIJtest123"))
                .andExpect(jsonPath("$[1].formattedAddress").value("123 Main Avenue, Chicago, IL 60601, USA"));
        
        verify(addressAutocompleteService, times(1)).getAddressSuggestions("123 Main");
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_EmptyResult() throws Exception {
        when(addressAutocompleteService.getAddressSuggestions("xyz nonexistent"))
                .thenReturn(List.of());
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "xyz nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(addressAutocompleteService, times(1)).getAddressSuggestions("xyz nonexistent");
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_RateLimitExceeded() throws Exception {
        when(addressAutocompleteService.getAddressSuggestions(anyString()))
                .thenThrow(new RateLimitExceededException());
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "123 Main"))
                .andExpect(status().isTooManyRequests());
    }
    
    @Test
    void testGetAddressSuggestions_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "123 Main"))
                .andExpect(status().isUnauthorized());
        
        verify(addressAutocompleteService, never()).getAddressSuggestions(anyString());
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_MissingPartialAddress() throws Exception {
        mockMvc.perform(get("/api/tasks/address-suggestions"))
                .andExpect(status().isBadRequest());
        
        verify(addressAutocompleteService, never()).getAddressSuggestions(anyString());
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_BlankPartialAddress() throws Exception {
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", ""))
                .andExpect(status().isBadRequest());
        
        verify(addressAutocompleteService, never()).getAddressSuggestions(anyString());
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_TooShortPartialAddress() throws Exception {
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "ab"))
                .andExpect(status().isBadRequest());
        
        verify(addressAutocompleteService, never()).getAddressSuggestions(anyString());
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_MinimumValidLength() throws Exception {
        when(addressAutocompleteService.getAddressSuggestions("abc"))
                .thenReturn(List.of());
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "abc"))
                .andExpect(status().isOk());
        
        verify(addressAutocompleteService, times(1)).getAddressSuggestions("abc");
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetAddressSuggestions_AsDispatcher() throws Exception {
        when(addressAutocompleteService.getAddressSuggestions("456 Oak"))
                .thenReturn(sampleSuggestions);
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "456 Oak"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        verify(addressAutocompleteService, times(1)).getAddressSuggestions("456 Oak");
    }
    
    @Test
    @WithMockUser(username = "admin@fsm.com", roles = {"ADMIN"})
    void testGetAddressSuggestions_AsAdmin() throws Exception {
        when(addressAutocompleteService.getAddressSuggestions("789 Pine"))
                .thenReturn(sampleSuggestions);
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "789 Pine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        verify(addressAutocompleteService, times(1)).getAddressSuggestions("789 Pine");
    }
    
    @Test
    @WithMockUser(username = "technician@fsm.com", roles = {"TECHNICIAN"})
    void testGetAddressSuggestions_AsTechnician() throws Exception {
        when(addressAutocompleteService.getAddressSuggestions("101 Elm"))
                .thenReturn(sampleSuggestions);
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "101 Elm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        verify(addressAutocompleteService, times(1)).getAddressSuggestions("101 Elm");
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_SingleResult() throws Exception {
        List<AddressSuggestionResponse> singleResult = List.of(
                AddressSuggestionResponse.builder()
                        .formattedAddress("1600 Amphitheatre Parkway, Mountain View, CA")
                        .latitude(37.4220)
                        .longitude(-122.0841)
                        .placeId("ChIJj61dQgK6j4AR4GeTYWZsKWw")
                        .build()
        );
        
        when(addressAutocompleteService.getAddressSuggestions("1600 Amphitheatre"))
                .thenReturn(singleResult);
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "1600 Amphitheatre"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].placeId").value("ChIJj61dQgK6j4AR4GeTYWZsKWw"));
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_SpecialCharacters() throws Exception {
        when(addressAutocompleteService.getAddressSuggestions("123 Main St, #100"))
                .thenReturn(List.of());
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", "123 Main St, #100"))
                .andExpect(status().isOk());
        
        verify(addressAutocompleteService, times(1)).getAddressSuggestions("123 Main St, #100");
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com")
    void testGetAddressSuggestions_LongAddress() throws Exception {
        String longAddress = "123 Very Long Street Name That Goes On And On Avenue";
        when(addressAutocompleteService.getAddressSuggestions(longAddress))
                .thenReturn(sampleSuggestions);
        
        mockMvc.perform(get("/api/tasks/address-suggestions")
                        .param("partialAddress", longAddress))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        verify(addressAutocompleteService, times(1)).getAddressSuggestions(longAddress);
    }
}
