package com.fsm.identity.smoke;

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
 * Smoke test for Identity Service
 * Tests: Health endpoint and basic user registration
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class IdentitySvcSmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static String testUserId;
    private static final String TEST_EMAIL = "smoke-test@example.com";

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
    @DisplayName("Should register a new user successfully")
    public void testUserRegistration() {
        // Given
        String url = "http://localhost:" + port + "/api/auth/register";
        
        Map<String, String> registrationRequest = new HashMap<>();
        registrationRequest.put("username", "smoketest");
        registrationRequest.put("email", TEST_EMAIL);
        registrationRequest.put("password", "TestPassword123!");
        registrationRequest.put("firstName", "Smoke");
        registrationRequest.put("lastName", "Test");
        registrationRequest.put("role", "TECHNICIAN");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(registrationRequest, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("id");
        
        testUserId = response.getBody().get("id").toString();
    }

    @Test
    @Order(4)
    @DisplayName("Should login with registered user")
    public void testUserLogin() {
        // Given
        String url = "http://localhost:" + port + "/api/auth/login";
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", TEST_EMAIL);
        loginRequest.put("password", "TestPassword123!");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("token");
    }
}
