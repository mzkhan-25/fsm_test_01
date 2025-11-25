package com.fsm.task.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for SecurityConfig
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testSwaggerUiIsAccessibleWithoutAuthentication() throws Exception {
        // Swagger UI should be accessible (any 2xx or 3xx status is acceptable)
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
    
    @Test
    void testApiDocsIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk());
    }
    
    @Test
    void testProtectedEndpointRequiresAuthentication() throws Exception {
        // Spring Security returns 403 for unauthenticated access to protected resources
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "DISPATCHER")
    void testProtectedEndpointAccessibleWithAuthentication() throws Exception {
        // GET endpoint for tasks should return 200 with authenticated user
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }
}
