package com.rsmad.backend.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiHealthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiHealthRequiresAuthInDefaultProfile() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthIsPublicInDefaultProfile() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }
}

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ApiHealthSecurityDevIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiHealthAllowsBasicAuthInDevProfile() throws Exception {
        mockMvc.perform(get("/api/health")
                        .with(httpBasic("rsmad", "rsmad")))
                .andExpect(status().isOk());
    }

    @Test
    void healthIsPublicInDevProfile() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }
}
