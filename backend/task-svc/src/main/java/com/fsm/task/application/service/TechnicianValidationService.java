package com.fsm.task.application.service;

import com.fsm.task.application.dto.TechnicianInfo;
import com.fsm.task.application.exception.TechnicianNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for validating technicians by calling the identity-svc.
 * Checks if a technician exists and is active before allowing task assignment.
 */
@Service
@Slf4j
public class TechnicianValidationService {
    
    private final RestTemplate restTemplate;
    private final String identityServiceUrl;
    private final boolean validationEnabled;
    private final boolean failOpenOnServiceUnavailable;
    
    /**
     * Creates a TechnicianValidationService with configurable behavior.
     * 
     * @param restTemplate the RestTemplate for HTTP calls
     * @param identityServiceUrl the URL of the identity-svc
     * @param validationEnabled whether to enable technician validation
     * @param failOpenOnServiceUnavailable when true, allows assignment if identity-svc is unavailable;
     *                                      when false, throws exception if service is unavailable
     */
    public TechnicianValidationService(
            RestTemplate restTemplate,
            @Value("${identity.service.url:http://localhost:8080}") String identityServiceUrl,
            @Value("${identity.service.validation.enabled:true}") boolean validationEnabled,
            @Value("${identity.service.fail-open:true}") boolean failOpenOnServiceUnavailable) {
        this.restTemplate = restTemplate;
        this.identityServiceUrl = identityServiceUrl;
        this.validationEnabled = validationEnabled;
        this.failOpenOnServiceUnavailable = failOpenOnServiceUnavailable;
    }
    
    /**
     * Validates that a technician exists and is active.
     * 
     * <p>Behavior when identity-svc is unavailable depends on the {@code identity.service.fail-open}
     * configuration property:</p>
     * <ul>
     *   <li>When {@code fail-open=true} (default): Logs a warning and allows the operation to proceed</li>
     *   <li>When {@code fail-open=false}: Throws a TechnicianNotFoundException</li>
     * </ul>
     * 
     * @param technicianId the ID of the technician to validate
     * @throws TechnicianNotFoundException if technician not found or inactive
     */
    public void validateTechnician(Long technicianId) {
        if (!validationEnabled) {
            log.debug("Technician validation is disabled, skipping validation for technician ID: {}", technicianId);
            return;
        }
        
        log.info("Validating technician with ID: {} against identity-svc at {}", technicianId, identityServiceUrl);
        
        try {
            String url = identityServiceUrl + "/api/users/" + technicianId;
            ResponseEntity<TechnicianInfo> response = restTemplate.getForEntity(url, TechnicianInfo.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TechnicianInfo technicianInfo = response.getBody();
                
                // Check if user is active
                if (!technicianInfo.isActive()) {
                    log.warn("Technician {} is not active (status: {})", technicianId, technicianInfo.getStatus());
                    throw new TechnicianNotFoundException(technicianId, "is not active");
                }
                
                log.info("Technician {} validated successfully: {} ({})", 
                        technicianId, technicianInfo.getName(), technicianInfo.getStatus());
            } else {
                log.warn("Unexpected response for technician {}: {}", technicianId, response.getStatusCode());
                throw new TechnicianNotFoundException(technicianId);
            }
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Technician not found in identity-svc: {}", technicianId);
                throw new TechnicianNotFoundException(technicianId);
            }
            // Re-throw other HTTP client errors as RestClientException
            throw e;
        } catch (RestClientException e) {
            log.error("Error calling identity-svc for technician {}: {}", technicianId, e.getMessage());
            if (failOpenOnServiceUnavailable) {
                // If identity-svc is unavailable and fail-open is enabled, log warning and proceed
                log.warn("Identity-svc unavailable (fail-open enabled), proceeding without technician validation for ID: {}", technicianId);
            } else {
                // If fail-open is disabled, throw exception to prevent assignment
                log.warn("Identity-svc unavailable (fail-open disabled), blocking assignment for technician ID: {}", technicianId);
                throw new TechnicianNotFoundException(technicianId, "could not be validated - identity service unavailable");
            }
        }
    }
    
    /**
     * Gets technician information from identity-svc.
     * 
     * @param technicianId the ID of the technician
     * @return TechnicianInfo or null if not found or service unavailable
     */
    public TechnicianInfo getTechnicianInfo(Long technicianId) {
        if (!validationEnabled) {
            log.debug("Technician validation is disabled, returning null for technician ID: {}", technicianId);
            return null;
        }
        
        try {
            String url = identityServiceUrl + "/api/users/" + technicianId;
            ResponseEntity<TechnicianInfo> response = restTemplate.getForEntity(url, TechnicianInfo.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            log.warn("Error fetching technician info for ID {}: {}", technicianId, e.getMessage());
        }
        return null;
    }
}
