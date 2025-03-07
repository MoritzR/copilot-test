package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void unauthenticatedUserCannotAccessCustomerAPIs() throws Exception {
        // Test GET endpoint
        mockMvc.perform(get("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());

        // Test POST endpoint
        mockMvc.perform(post("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\"}"))
                .andExpect(status().isUnauthorized());

        // Test specific customer endpoint
        mockMvc.perform(get("/api/customers/" + UUID.randomUUID())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCannotDeleteCustomers() throws Exception {
        mockMvc.perform(delete("/api/customers/" + UUID.randomUUID())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleCanDeleteCustomers() throws Exception {
        // Note: This test might return 404 if the customer doesn't exist, 
        // but it should not return 401 or 403
        mockMvc.perform(delete("/api/customers/" + UUID.randomUUID())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void authenticatedUserCanAccessCustomerEndpoints() throws Exception {
        mockMvc.perform(get("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }
}
