package com.layerten.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for SecurityConfig.
 * Tests authentication and authorization rules.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "layerten.admin.username=testadmin",
    "layerten.admin.password=testpass",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
class SecurityConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void publicEndpoints_shouldBeAccessibleWithoutAuthentication() throws Exception {
        // Public list endpoint should be accessible
        mockMvc.perform(get("/api/lists"))
            .andExpect(status().isOk());
        
        // Public post endpoint should be accessible
        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk());
        
        // Public suggestion endpoint should accept POST (validation will fail but not auth)
        mockMvc.perform(post("/api/suggestions")
            .contentType("application/json")
            .content("{}"))
            .andExpect(status().isBadRequest()); // 400 due to validation, not 401
    }
    
    @Test
    void adminEndpoints_shouldRequireAuthentication() throws Exception {
        // Admin endpoints should return 401 without authentication
        mockMvc.perform(get("/api/admin/lists"))
            .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/admin/posts"))
            .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/admin/suggestions"))
            .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/admin/media")
            .contentType("multipart/form-data"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void adminEndpoints_shouldBeAccessibleWithValidCredentials() throws Exception {
        // Admin endpoints should be accessible with valid credentials
        mockMvc.perform(get("/api/admin/suggestions")
            .with(httpBasic("testadmin", "testpass")))
            .andExpect(status().isOk());
    }
    
    @Test
    void adminEndpoints_shouldRejectInvalidCredentials() throws Exception {
        // Admin endpoints should reject invalid credentials
        mockMvc.perform(get("/api/admin/suggestions")
            .with(httpBasic("wronguser", "wrongpass")))
            .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/admin/suggestions")
            .with(httpBasic("testadmin", "wrongpass")))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoints_shouldBeAccessibleWithMockUser() throws Exception {
        // Test with mock user for unit testing
        mockMvc.perform(get("/api/admin/suggestions"))
            .andExpect(status().isOk());
    }
    
    @Test
    void mediaEndpoints_shouldBePubliclyAccessible() throws Exception {
        // Media serving endpoint should be public (will return 404 for non-existent media)
        mockMvc.perform(get("/api/media/999"))
            .andExpect(status().isNotFound()); // 404, not 401
    }
    
    @Test
    void rootPath_shouldBeDenied() throws Exception {
        // Root path should be denied by security (no controller mapped)
        mockMvc.perform(get("/"))
            .andExpect(status().is4xxClientError()); // Either 403 or 404
    }
    
    @Test
    void unmappedPaths_shouldBeDenied() throws Exception {
        // Unmapped paths should be denied by security
        mockMvc.perform(get("/some/random/path"))
            .andExpect(status().is4xxClientError()); // Either 403 or 404
    }
}
