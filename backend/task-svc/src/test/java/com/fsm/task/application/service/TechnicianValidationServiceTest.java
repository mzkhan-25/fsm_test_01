package com.fsm.task.application.service;

import com.fsm.task.application.dto.TechnicianInfo;
import com.fsm.task.application.exception.TechnicianNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TechnicianValidationService
 */
@ExtendWith(MockitoExtension.class)
class TechnicianValidationServiceTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    private TechnicianValidationService validationService;
    private TechnicianValidationService disabledValidationService;
    private TechnicianValidationService failClosedValidationService;
    
    private static final String IDENTITY_SERVICE_URL = "http://localhost:8080";
    
    @BeforeEach
    void setUp() {
        // Default: validation enabled, fail-open enabled
        validationService = new TechnicianValidationService(restTemplate, IDENTITY_SERVICE_URL, true, true);
        // Validation disabled
        disabledValidationService = new TechnicianValidationService(restTemplate, IDENTITY_SERVICE_URL, false, true);
        // Fail-closed: throws exception when service unavailable
        failClosedValidationService = new TechnicianValidationService(restTemplate, IDENTITY_SERVICE_URL, true, false);
    }
    
    @Test
    void testValidateTechnicianSuccess() {
        Long technicianId = 101L;
        TechnicianInfo technicianInfo = TechnicianInfo.builder()
                .id(technicianId)
                .name("John Doe")
                .email("john.doe@fsm.com")
                .role("TECHNICIAN")
                .status("ACTIVE")
                .build();
        
        ResponseEntity<TechnicianInfo> response = new ResponseEntity<>(technicianInfo, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(TechnicianInfo.class))).thenReturn(response);
        
        // Should not throw any exception
        assertDoesNotThrow(() -> validationService.validateTechnician(technicianId));
        
        verify(restTemplate).getForEntity(
                eq(IDENTITY_SERVICE_URL + "/api/users/" + technicianId),
                eq(TechnicianInfo.class)
        );
    }
    
    @Test
    void testValidateTechnicianNotFound() {
        Long technicianId = 999L;
        
        when(restTemplate.getForEntity(anyString(), eq(TechnicianInfo.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        TechnicianNotFoundException exception = assertThrows(
                TechnicianNotFoundException.class,
                () -> validationService.validateTechnician(technicianId)
        );
        
        assertEquals(technicianId, exception.getTechnicianId());
        assertTrue(exception.getMessage().contains("not found"));
    }
    
    @Test
    void testValidateTechnicianInactive() {
        Long technicianId = 102L;
        TechnicianInfo inactiveTechnician = TechnicianInfo.builder()
                .id(technicianId)
                .name("Jane Smith")
                .email("jane.smith@fsm.com")
                .role("TECHNICIAN")
                .status("INACTIVE")
                .build();
        
        ResponseEntity<TechnicianInfo> response = new ResponseEntity<>(inactiveTechnician, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(TechnicianInfo.class))).thenReturn(response);
        
        TechnicianNotFoundException exception = assertThrows(
                TechnicianNotFoundException.class,
                () -> validationService.validateTechnician(technicianId)
        );
        
        assertEquals(technicianId, exception.getTechnicianId());
        assertTrue(exception.getMessage().contains("not active"));
    }
    
    @Test
    void testValidateTechnicianServiceUnavailable() {
        Long technicianId = 103L;
        
        when(restTemplate.getForEntity(anyString(), eq(TechnicianInfo.class)))
                .thenThrow(new RestClientException("Connection refused"));
        
        // Should not throw - service unavailable is handled gracefully (fail-open)
        assertDoesNotThrow(() -> validationService.validateTechnician(technicianId));
    }
    
    @Test
    void testValidateTechnicianServiceUnavailableFailClosed() {
        Long technicianId = 103L;
        
        when(restTemplate.getForEntity(anyString(), eq(TechnicianInfo.class)))
                .thenThrow(new RestClientException("Connection refused"));
        
        // Should throw when fail-closed is configured
        TechnicianNotFoundException exception = assertThrows(
                TechnicianNotFoundException.class,
                () -> failClosedValidationService.validateTechnician(technicianId)
        );
        
        assertEquals(technicianId, exception.getTechnicianId());
        assertTrue(exception.getMessage().contains("could not be validated"));
    }
    
    @Test
    void testValidateTechnicianValidationDisabled() {
        Long technicianId = 104L;
        
        // Should not throw and should not call RestTemplate
        assertDoesNotThrow(() -> disabledValidationService.validateTechnician(technicianId));
        
        verify(restTemplate, never()).getForEntity(anyString(), any());
    }
    
    @Test
    void testGetTechnicianInfoSuccess() {
        Long technicianId = 105L;
        TechnicianInfo technicianInfo = TechnicianInfo.builder()
                .id(technicianId)
                .name("Bob Wilson")
                .email("bob.wilson@fsm.com")
                .role("TECHNICIAN")
                .status("ACTIVE")
                .build();
        
        ResponseEntity<TechnicianInfo> response = new ResponseEntity<>(technicianInfo, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(TechnicianInfo.class))).thenReturn(response);
        
        TechnicianInfo result = validationService.getTechnicianInfo(technicianId);
        
        assertNotNull(result);
        assertEquals(technicianId, result.getId());
        assertEquals("Bob Wilson", result.getName());
        assertTrue(result.isActive());
    }
    
    @Test
    void testGetTechnicianInfoNotFound() {
        Long technicianId = 999L;
        
        when(restTemplate.getForEntity(anyString(), eq(TechnicianInfo.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        TechnicianInfo result = validationService.getTechnicianInfo(technicianId);
        
        assertNull(result);
    }
    
    @Test
    void testGetTechnicianInfoValidationDisabled() {
        Long technicianId = 106L;
        
        TechnicianInfo result = disabledValidationService.getTechnicianInfo(technicianId);
        
        assertNull(result);
        verify(restTemplate, never()).getForEntity(anyString(), any());
    }
    
    @Test
    void testTechnicianInfoIsActive() {
        TechnicianInfo activeTech = TechnicianInfo.builder().status("ACTIVE").build();
        TechnicianInfo inactiveTech = TechnicianInfo.builder().status("INACTIVE").build();
        TechnicianInfo nullStatus = TechnicianInfo.builder().status(null).build();
        
        assertTrue(activeTech.isActive());
        assertFalse(inactiveTech.isActive());
        assertFalse(nullStatus.isActive());
    }
    
    @Test
    void testTechnicianInfoIsTechnician() {
        TechnicianInfo technician = TechnicianInfo.builder().role("TECHNICIAN").build();
        TechnicianInfo dispatcher = TechnicianInfo.builder().role("DISPATCHER").build();
        TechnicianInfo nullRole = TechnicianInfo.builder().role(null).build();
        
        assertTrue(technician.isTechnician());
        assertFalse(dispatcher.isTechnician());
        assertFalse(nullRole.isTechnician());
    }
    
    @Test
    void testValidateTechnicianNullResponse() {
        Long technicianId = 107L;
        
        ResponseEntity<TechnicianInfo> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(TechnicianInfo.class))).thenReturn(response);
        
        TechnicianNotFoundException exception = assertThrows(
                TechnicianNotFoundException.class,
                () -> validationService.validateTechnician(technicianId)
        );
        
        assertEquals(technicianId, exception.getTechnicianId());
    }
}
