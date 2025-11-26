package com.fsm.task.smoke;

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
 * Smoke test for Task Service
 * Tests: Health endpoint and basic task operations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TaskSvcSmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static String testTaskId;

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
    @DisplayName("Should create a new task successfully")
    public void testCreateTask() {
        // Given
        String url = "http://localhost:" + port + "/api/tasks";
        
        Map<String, Object> taskRequest = new HashMap<>();
        taskRequest.put("title", "Smoke Test Task");
        taskRequest.put("description", "This is a smoke test task");
        taskRequest.put("customerId", "test-customer-123");
        taskRequest.put("priority", "MEDIUM");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(taskRequest, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("id");
        
        testTaskId = response.getBody().get("id").toString();
    }

    @Test
    @Order(4)
    @DisplayName("Should retrieve the created task")
    public void testGetTask() {
        // Ensure we have a task ID
        assertThat(testTaskId).isNotNull();

        // Given
        String url = "http://localhost:" + port + "/api/tasks/" + testTaskId;

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id").toString()).isEqualTo(testTaskId);
        assertThat(response.getBody().get("title")).isEqualTo("Smoke Test Task");
    }

    @Test
    @Order(5)
    @DisplayName("Should list all tasks")
    public void testListTasks() {
        // Given
        String url = "http://localhost:" + port + "/api/tasks";

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
