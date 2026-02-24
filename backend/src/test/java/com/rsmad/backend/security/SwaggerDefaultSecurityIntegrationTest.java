package com.rsmad.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerDefaultSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerUiIsNotAccessibleInDefaultProfile() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(result -> {
                    if (result.getResponse().getStatus() == 200) {
                        throw new AssertionError("Expected non-200 for /swagger-ui/index.html");
                    }
                });
    }

    @Test
    void apiDocsIsNotAccessibleInDefaultProfile() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(result -> {
                    if (result.getResponse().getStatus() == 200) {
                        throw new AssertionError("Expected non-200 for /v3/api-docs");
                    }
                });
    }

    @Test
    void healthIsPublicInDefaultProfile() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }
}
