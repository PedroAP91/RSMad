package com.rsmad.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmad.backend.exception.GlobalExceptionHandler;
import com.rsmad.backend.mapper.RequestMapper;
import com.rsmad.backend.mapper.ResourceMapper;
import com.rsmad.backend.model.Request;
import com.rsmad.backend.model.RequestStatus;
import com.rsmad.backend.model.RequestType;
import com.rsmad.backend.model.Resource;
import com.rsmad.backend.repository.RequestRepository;
import com.rsmad.backend.repository.ResourceRepository;
import com.rsmad.backend.service.RequestService;
import com.rsmad.backend.service.ResourceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(ResourceRequestsController.class)
@Import({
        RequestService.class,
        RequestRepository.class,
        RequestMapper.class,
        ResourceService.class,
        ResourceRepository.class,
        ResourceMapper.class,
        GlobalExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class ResourceRequestsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @BeforeEach
    void resetInMemoryStore() {
        requestRepository.clear();
        resourceRepository.clear();
    }

    @Test
    void getRequestsByResourceReturnsPagedItemsWhenResourceExists() throws Exception {
        Resource resource = resourceRepository.create(new Resource(null, "Comedor A"));
        Request r1 = requestRepository.create(new Request(null, "Uno", "d1", null, null, null, null, RequestType.COMIDA, null, null, null));
        Request r2 = requestRepository.create(new Request(null, "Dos", "d2", null, null, null, null, RequestType.SALUD, null, null, null));
        Request r3 = requestRepository.create(new Request(null, "Tres", "d3", null, null, null, null, RequestType.OTROS, null, null, null));
        requestRepository.updateResourceId(r1.id(), resource.id());
        requestRepository.updateResourceId(r2.id(), resource.id());
        requestRepository.updateResourceId(r3.id(), resource.id());

        MvcResult result = mockMvc.perform(get("/api/resources/{id}/requests?page=0&size=2", resource.id()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(3);
        assertThat(body.get("items")).hasSize(2);
    }

    @Test
    void getRequestsByResourceExcludesRequestsFromOtherResources() throws Exception {
        Resource resourceA = resourceRepository.create(new Resource(null, "A"));
        Resource resourceB = resourceRepository.create(new Resource(null, "B"));
        Request rA = requestRepository.create(new Request(null, "Req A", "d1", null, null, null, null, RequestType.COMIDA, null, null, null));
        Request rB = requestRepository.create(new Request(null, "Req B", "d2", null, null, null, null, RequestType.SALUD, null, null, null));
        requestRepository.updateResourceId(rA.id(), resourceA.id());
        requestRepository.updateResourceId(rB.id(), resourceB.id());

        MvcResult result = mockMvc.perform(get("/api/resources/{id}/requests", resourceA.id()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(1);
        assertThat(body.get("items")).hasSize(1);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(rA.id());
    }

    @Test
    void getRequestsByResourceReturns404WhenResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/resources/{id}/requests", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestsByResourceAppliesFiltersAndWithResourceId() throws Exception {
        Resource resource = resourceRepository.create(new Resource(null, "A"));
        Request openWithMatch = requestRepository.create(
                new Request(null, "Necesito ayuda", "en calle principal", null, null, null, null, RequestType.COMIDA, null, null, null)
        );
        Request closedWithMatch = requestRepository.create(
                new Request(null, "Necesito ayuda", "en calle secundaria", null, null, null, null, RequestType.COMIDA, null, null, null)
        );
        Request openNoMatch = requestRepository.create(
                new Request(null, "Necesito ayuda", "sin coincidencia", null, null, null, null, RequestType.COMIDA, null, null, null)
        );
        requestRepository.updateResourceId(openWithMatch.id(), resource.id());
        requestRepository.updateResourceId(closedWithMatch.id(), resource.id());
        requestRepository.updateResourceId(openNoMatch.id(), resource.id());
        requestRepository.updateStatus(closedWithMatch.id(), RequestStatus.CERRADA);

        MvcResult result = mockMvc.perform(get("/api/resources/{id}/requests?status=ABIERTA&q=calle", resource.id()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(1);
        assertThat(body.get("items")).hasSize(1);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(openWithMatch.id());
    }
}
