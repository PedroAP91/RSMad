package com.rsmad.backend.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DefaultProfileSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthIsPublicWithoutAuth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void apiIsUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiRejectsDevCredentialsInDefaultProfile() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .with(httpBasic("rsmad", "rsmad")))
                .andExpect(result -> {
                    int responseStatus = result.getResponse().getStatus();
                    if (responseStatus != 401 && responseStatus != 403) {
                        throw new AssertionError("Expected 401 or 403, but got " + responseStatus);
                    }
                });
    }
}
