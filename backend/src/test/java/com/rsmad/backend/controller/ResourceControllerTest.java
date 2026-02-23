package com.rsmad.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmad.backend.exception.GlobalExceptionHandler;
import com.rsmad.backend.mapper.ResourceMapper;
import com.rsmad.backend.repository.ResourceRepository;
import com.rsmad.backend.service.ResourceService;

import java.util.List;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

@WebMvcTest(ResourceController.class)
@Import({ResourceService.class, ResourceRepository.class, ResourceMapper.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceRepository resourceRepository;

    @BeforeEach
    void resetInMemoryStore() {
        resourceRepository.clear();
    }

    private long createResource(String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "%s"
                                }
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    @Test
    void createResourceReturns201AndLocationAndBody() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Recurso prueba"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/resources/\\d+")))
                .andExpect(jsonPath("$.nombre").value("Recurso prueba"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(result.getResponse().getHeader("Location"))
                .isEqualTo("/api/resources/" + body.get("id").asLong());
    }

    @Test
    void createResourceWithInvalidNombreReturns400() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void getResourceByIdReturns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/resources/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getResourcesReturnsListOrderedByIdAscAndSize3() throws Exception {
        createResource("Uno");
        createResource("Dos");
        createResource("Tres");

        MvcResult result = mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        List<Long> ids = StreamSupport.stream(body.spliterator(), false)
                .map(node -> node.get("id").asLong())
                .toList();

        assertThat(ids).hasSize(3);
        assertThat(ids).isSorted();
    }

    @Test
    void deleteResourceReturns204AndResourceIsGone() throws Exception {
        long createdId = createResource("Para borrar");

        mockMvc.perform(delete("/api/resources/{id}", createdId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/resources/{id}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteResourceReturns204WhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/resources/{id}", 999999))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateResourceReturns200WhenUpdated() throws Exception {
        long createdId = createResource("Inicial");

        mockMvc.perform(put("/api/resources/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Actualizado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.nombre").value("Actualizado"));
    }

    @Test
    void updateResourceReturns400WhenInvalid() throws Exception {
        long createdId = createResource("Valido");
        mockMvc.perform(put("/api/resources/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateResourceReturns404WhenNotFound() throws Exception {
        mockMvc.perform(put("/api/resources/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "No existe"
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
