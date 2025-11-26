package com.fsm.location.smoke;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test for Location Service
 * Tests: Health endpoint and basic location operations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class LocationSvcSmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static String testLocationId;
    private static final String TEST_TECHNICIAN_ID = "tech-123";

    @Test
    @Order(1)
    @DisplayName("Health check endpoint should return 200 OK")
    public void testHealthCheck() {
        // Given
        String url = "http://localhost:" + port + "/actuator/health";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @Order(2)
    @DisplayName("API docs endpoint should be accessible")
    public void testApiDocs() {
        // Given
        String url = "http://localhost:" + port + "/api-docs";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(3)
    @DisplayName("Should update technician location successfully")
    public void testUpdateLocation() {
        // Given
        String url = "http://localhost:" + port + "/api/locations";
        
        Map<String, Object> locationRequest = new HashMap<>();
        locationRequest.put("technicianId", TEST_TECHNICIAN_ID);
        locationRequest.put("latitude", 37.7749);
        locationRequest.put("longitude", -122.4194);
        locationRequest.put("accuracy", 10.0);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(locationRequest, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("id");
        
        testLocationId = response.getBody().get("id").toString();
    }

    @Test
    @Order(4)
    @DisplayName("Should retrieve technician's latest location")
    public void testGetLatestLocation() {
        // Given
        String url = "http://localhost:" + port + "/api/locations/technician/" + TEST_TECHNICIAN_ID + "/latest";

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("technicianId")).isEqualTo(TEST_TECHNICIAN_ID);
    }

    @Test
    @Order(5)
    @DisplayName("Should retrieve technician's location history")
    public void testGetLocationHistory() {
        // Given
        String url = "http://localhost:" + port + "/api/locations/technician/" + TEST_TECHNICIAN_ID + "/history";

        // When
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
