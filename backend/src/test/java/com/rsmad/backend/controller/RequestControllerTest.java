package com.rsmad.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmad.backend.exception.GlobalExceptionHandler;
import com.rsmad.backend.mapper.RequestMapper;
import com.rsmad.backend.model.Resource;
import com.rsmad.backend.repository.RequestRepository;
import com.rsmad.backend.repository.ResourceRepository;
import com.rsmad.backend.service.ResourceService;
import com.rsmad.backend.service.RequestService;

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

@WebMvcTest(RequestController.class)
@Import({RequestService.class, RequestRepository.class, RequestMapper.class, ResourceService.class, ResourceRepository.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class RequestControllerTest {

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

    private long createRequest(String titulo, String descripcion, String tipo) throws Exception {
        return createRequest(titulo, descripcion, tipo, null, null, null);
    }

    private long createRequest(
            String titulo,
            String descripcion,
            String tipo,
            String contactPhone,
            String district,
            String notes
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "%s",
                                  "descripcion": "%s",
                                  "contactPhone": %s,
                                  "district": %s,
                                  "notes": %s,
                                  "tipo": "%s"
                                }
                                """.formatted(
                                titulo,
                                descripcion,
                                quoteOrNull(contactPhone),
                                quoteOrNull(district),
                                quoteOrNull(notes),
                                tipo
                        )))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String quoteOrNull(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    @Test
    void createRequestReturns201AndLocationAndBody() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Necesito comida",
                                  "descripcion": "Para tres personas",
                                  "contactPhone": "+34 666 555 444",
                                  "district": "Centro",
                                  "notes": "Tiene alergias",
                                  "tipo": "COMIDA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/requests/\\d+")))
                .andExpect(jsonPath("$.titulo").value("Necesito comida"))
                .andExpect(jsonPath("$.descripcion").value("Para tres personas"))
                .andExpect(jsonPath("$.contactPhone").value("+34666555444"))
                .andExpect(jsonPath("$.district").value("Centro"))
                .andExpect(jsonPath("$.notes").value("Tiene alergias"))
                .andExpect(jsonPath("$.tipo").value("COMIDA"))
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(result.getResponse().getHeader("Location"))
                .isEqualTo("/api/requests/" + body.get("id").asLong());
        assertThat(body.get("createdAt").asText()).isNotBlank();
        assertThat(body.get("updatedAt").asText()).isNotBlank();
    }

    @Test
    void createRequestWithInvalidTituloReturns400() throws Exception {
        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "",
                                  "descripcion": "Descripcion",
                                  "tipo": "COMIDA"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void createRequestWithInvalidContactPhoneReturns400() throws Exception {
        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Req",
                                  "descripcion": "Desc",
                                  "contactPhone": "abc",
                                  "tipo": "COMIDA"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequestWithTooLongNotesReturns400() throws Exception {
        String longNotes = "a".repeat(501);
        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Req",
                                  "descripcion": "Desc",
                                  "notes": "%s",
                                  "tipo": "COMIDA"
                                }
                                """.formatted(longNotes)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsReturnsWrapperOrderedByIdAscAndSize3() throws Exception {
        createRequest("Req Uno", "d1", "COMIDA");
        createRequest("Req Dos", "d2", "SALUD");
        createRequest("Req Tres", "d3", "OTROS");

        MvcResult result = mockMvc.perform(get("/api/requests"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("page").asInt()).isEqualTo(0);
        assertThat(body.get("size").asInt()).isEqualTo(20);
        assertThat(body.get("total").asInt()).isEqualTo(3);

        List<Long> ids = StreamSupport.stream(body.get("items").spliterator(), false)
                .map(node -> node.get("id").asLong())
                .toList();

        assertThat(ids).hasSize(3);
        assertThat(ids).isSorted();
    }

    @Test
    void getRequestsWithStatusFilterReturnsOnlyMatchingStatus() throws Exception {
        long abierta = createRequest("Abierta", "d1", "COMIDA");
        long enProceso = createRequest("En proceso", "d2", "SALUD");
        createRequest("Otra abierta", "d3", "OTROS");

        mockMvc.perform(patch("/api/requests/{id}/status", enProceso)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "EN_PROCESO"
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/requests?status=ABIERTA"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(2);
        assertThat(body.get("items")).hasSize(2);
        assertThat(StreamSupport.stream(body.get("items").spliterator(), false)
                .allMatch(node -> "ABIERTA".equals(node.get("estado").asText()))).isTrue();
        assertThat(StreamSupport.stream(body.get("items").spliterator(), false)
                .map(node -> node.get("id").asLong()).toList()).contains(abierta);
    }

    @Test
    void getRequestsWithTypeFilterReturnsOnlyMatchingType() throws Exception {
        createRequest("Comida 1", "d1", "COMIDA");
        createRequest("Salud", "d2", "SALUD");
        createRequest("Comida 2", "d3", "COMIDA");

        MvcResult result = mockMvc.perform(get("/api/requests?type=COMIDA"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(2);
        assertThat(body.get("items")).hasSize(2);
        assertThat(StreamSupport.stream(body.get("items").spliterator(), false)
                .allMatch(node -> "COMIDA".equals(node.get("tipo").asText()))).isTrue();
    }

    @Test
    void getRequestsWithStatusAndTypeFilterReturnsOnlyMatchingBoth() throws Exception {
        long abiertaComida = createRequest("Abierta comida", "d1", "COMIDA");
        createRequest("Abierta salud", "d2", "SALUD");
        long cerradaComida = createRequest("Cerrada comida", "d3", "COMIDA");

        mockMvc.perform(patch("/api/requests/{id}/status", cerradaComida)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "CERRADA"
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/requests?status=ABIERTA&type=COMIDA"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(1);
        assertThat(body.get("items")).hasSize(1);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(abiertaComida);
        assertThat(body.get("items").get(0).get("estado").asText()).isEqualTo("ABIERTA");
        assertThat(body.get("items").get(0).get("tipo").asText()).isEqualTo("COMIDA");
    }

    @Test
    void getRequestsWithQueryFiltersByTitulo() throws Exception {
        createRequest("Necesito comida urgente", "d1", "COMIDA");
        createRequest("Necesito duchas", "d2", "DUCHAS");
        createRequest("COMIDA para familia", "d3", "OTROS");

        MvcResult result = mockMvc.perform(get("/api/requests?q=comida"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(2);
        assertThat(body.get("items")).hasSize(2);
        assertThat(StreamSupport.stream(body.get("items").spliterator(), false)
                .allMatch(node -> node.get("titulo").asText().toLowerCase().contains("comida"))).isTrue();
    }

    @Test
    void getRequestsWithQueryFiltersByDescripcion() throws Exception {
        createRequest("Solicitud 1", "Vivo en la calle", "COMIDA");
        createRequest("Solicitud 2", "Sin techo en CALLE central", "DUCHAS");
        createRequest("Solicitud 3", "Sin coincidencia", "OTROS");

        MvcResult result = mockMvc.perform(get("/api/requests?q=calle"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(2);
        assertThat(body.get("items")).hasSize(2);
    }

    @Test
    void getRequestsWithQueryAndStatusAndTypeAppliesAndLogic() throws Exception {
        long abiertaComidaConMatch = createRequest("Ayuda", "Necesito comida en calle", "COMIDA");
        long abiertaSaludConMatch = createRequest("Ayuda", "Necesito comida en calle", "SALUD");
        long cerradaComidaConMatch = createRequest("Ayuda", "Necesito comida en calle", "COMIDA");
        createRequest("Ayuda", "Texto sin match", "COMIDA");

        mockMvc.perform(patch("/api/requests/{id}/status", cerradaComidaConMatch)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "CERRADA"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/requests/{id}/status", abiertaSaludConMatch)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "CERRADA"
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/requests?status=ABIERTA&type=COMIDA&q=calle"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(1);
        assertThat(body.get("items")).hasSize(1);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(abiertaComidaConMatch);
    }

    @Test
    void getRequestsWithBlankQueryDoesNotFilter() throws Exception {
        createRequest("Uno", "desc uno", "COMIDA");
        createRequest("Dos", "desc dos", "SALUD");
        createRequest("Tres", "desc tres", "OTROS");

        MvcResult result = mockMvc.perform(get("/api/requests").param("q", "   "))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(3);
        assertThat(body.get("items")).hasSize(3);
    }

    @Test
    void getRequestsWithResourceIdFilterReturnsOnlyAssignedToThatResource() throws Exception {
        long request1 = createRequest("Req1", "d1", "COMIDA");
        long request2 = createRequest("Req2", "d2", "SALUD");
        createRequest("Req3", "d3", "OTROS");

        Resource resourceA = resourceRepository.create(new Resource(null, "Resource A"));
        Resource resourceB = resourceRepository.create(new Resource(null, "Resource B"));

        mockMvc.perform(patch("/api/requests/{id}/assign-resource", request1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resourceId": %d}
                                """.formatted(resourceA.id())))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/requests/{id}/assign-resource", request2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resourceId": %d}
                                """.formatted(resourceB.id())))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/requests?resourceId=%d".formatted(resourceA.id())))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(1);
        assertThat(body.get("items")).hasSize(1);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(request1);
        assertThat(body.get("items").get(0).get("resourceId").asLong()).isEqualTo(resourceA.id());
    }

    @Test
    void getRequestsWithResourceIdAndStatusAppliesAndLogic() throws Exception {
        long requestOpen = createRequest("Req open", "d1", "COMIDA");
        long requestClosed = createRequest("Req closed", "d2", "SALUD");

        Resource resourceA = resourceRepository.create(new Resource(null, "Resource A"));

        mockMvc.perform(patch("/api/requests/{id}/assign-resource", requestOpen)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resourceId": %d}
                                """.formatted(resourceA.id())))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/requests/{id}/assign-resource", requestClosed)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resourceId": %d}
                                """.formatted(resourceA.id())))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/requests/{id}/status", requestClosed)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado": "CERRADA"}
                                """))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/requests?resourceId=%d&status=ABIERTA".formatted(resourceA.id())))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(1);
        assertThat(body.get("items")).hasSize(1);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(requestOpen);
    }

    @Test
    void getRequestsWithNonExistingResourceIdReturns404() throws Exception {
        mockMvc.perform(get("/api/requests?resourceId=999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestsWithInvalidResourceIdReturns400() throws Exception {
        mockMvc.perform(get("/api/requests?resourceId=0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/requests?resourceId=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsWithSortCreatedAtDescReturnsExpectedOrder() throws Exception {
        createRequest("Uno", "d1", "COMIDA");
        Thread.sleep(2);
        createRequest("Dos", "d2", "SALUD");
        Thread.sleep(2);
        createRequest("Tres", "d3", "OTROS");

        MvcResult result = mockMvc.perform(get("/api/requests?sort=createdAt,desc"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("items")).hasSize(3);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(3);
        assertThat(body.get("items").get(1).get("id").asLong()).isEqualTo(2);
        assertThat(body.get("items").get(2).get("id").asLong()).isEqualTo(1);
    }

    @Test
    void getRequestsWithSortTituloAscOrdersAlphabetically() throws Exception {
        createRequest("Zeta", "d1", "COMIDA");
        createRequest("Lima", "d2", "SALUD");
        createRequest("Alfa", "d3", "OTROS");

        MvcResult result = mockMvc.perform(get("/api/requests?sort=titulo,asc"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("items")).hasSize(3);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(3);
        assertThat(body.get("items").get(1).get("id").asLong()).isEqualTo(2);
        assertThat(body.get("items").get(2).get("id").asLong()).isEqualTo(1);
    }

    @Test
    void getRequestsWithInvalidSortFieldReturns400() throws Exception {
        mockMvc.perform(get("/api/requests?sort=foo,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsWithInvalidSortDirectionReturns400() throws Exception {
        mockMvc.perform(get("/api/requests?sort=id,up"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsPaginationPage0Size2ReturnsFirstTwo() throws Exception {
        createRequest("Uno", "d1", "COMIDA");
        createRequest("Dos", "d2", "SALUD");
        createRequest("Tres", "d3", "OTROS");
        createRequest("Cuatro", "d4", "DUCHAS");
        createRequest("Cinco", "d5", "ALOJAMIENTO");

        MvcResult result = mockMvc.perform(get("/api/requests?page=0&size=2"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(5);
        assertThat(body.get("items")).hasSize(2);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(1);
        assertThat(body.get("items").get(1).get("id").asLong()).isEqualTo(2);
    }

    @Test
    void getRequestsPaginationPage1Size2ReturnsThirdAndFourth() throws Exception {
        createRequest("Uno", "d1", "COMIDA");
        createRequest("Dos", "d2", "SALUD");
        createRequest("Tres", "d3", "OTROS");
        createRequest("Cuatro", "d4", "DUCHAS");
        createRequest("Cinco", "d5", "ALOJAMIENTO");

        MvcResult result = mockMvc.perform(get("/api/requests?page=1&size=2"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(5);
        assertThat(body.get("items")).hasSize(2);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(3);
        assertThat(body.get("items").get(1).get("id").asLong()).isEqualTo(4);
    }

    @Test
    void getRequestsPaginationOutOfRangeReturnsEmptyItems() throws Exception {
        createRequest("Uno", "d1", "COMIDA");
        createRequest("Dos", "d2", "SALUD");
        createRequest("Tres", "d3", "OTROS");
        createRequest("Cuatro", "d4", "DUCHAS");
        createRequest("Cinco", "d5", "ALOJAMIENTO");

        MvcResult result = mockMvc.perform(get("/api/requests?page=10&size=2"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(5);
        assertThat(body.get("items")).isEmpty();
    }

    @Test
    void getRequestsWithNegativePageReturns400() throws Exception {
        mockMvc.perform(get("/api/requests?page=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsWithZeroSizeReturns400() throws Exception {
        mockMvc.perform(get("/api/requests?size=0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsWithStatusAndPaginationReturnsExpectedPageAndTotal() throws Exception {
        createRequest("Abierta uno", "d1", "COMIDA");
        long abiertaDos = createRequest("Abierta dos", "d2", "SALUD");
        long cerrada = createRequest("Cerrada", "d3", "OTROS");

        mockMvc.perform(patch("/api/requests/{id}/status", cerrada)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "CERRADA"
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/requests?status=ABIERTA&page=1&size=1"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("total").asInt()).isEqualTo(2);
        assertThat(body.get("items")).hasSize(1);
        assertThat(body.get("items").get(0).get("id").asLong()).isEqualTo(abiertaDos);
    }

    @Test
    void getRequestsWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/requests?status=NOPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestByIdReturns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/requests/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRequestReturns200WhenUpdated() throws Exception {
        long createdId = createRequest("Inicial", "desc", "COMIDA", "+34 611 222 333", "Centro", "nota inicial");

        mockMvc.perform(put("/api/requests/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Actualizado",
                                  "descripcion": "desc nueva",
                                  "contactPhone": "+34 699 888 777",
                                  "district": "Retiro",
                                  "notes": "nota actualizada",
                                  "tipo": "SALUD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.titulo").value("Actualizado"))
                .andExpect(jsonPath("$.descripcion").value("desc nueva"))
                .andExpect(jsonPath("$.contactPhone").value("+34699888777"))
                .andExpect(jsonPath("$.district").value("Retiro"))
                .andExpect(jsonPath("$.notes").value("nota actualizada"))
                .andExpect(jsonPath("$.tipo").value("SALUD"))
                .andExpect(jsonPath("$.estado").value("ABIERTA"));
    }

    @Test
    void updateRequestReturns404WhenNotFound() throws Exception {
        mockMvc.perform(put("/api/requests/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "No existe",
                                  "descripcion": "x",
                                  "tipo": "COMIDA"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRequestStatusReturns200WhenUpdated() throws Exception {
        long createdId = createRequest("Estado", "desc", "COMIDA");

        mockMvc.perform(patch("/api/requests/{id}/status", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "EN_PROCESO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    void updateRequestStatusReturns404WhenNotFound() throws Exception {
        mockMvc.perform(patch("/api/requests/{id}/status", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "EN_PROCESO"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignResourceReturns200WhenUpdated() throws Exception {
        long requestId = createRequest("Req", "desc", "COMIDA");
        Resource createdResource = resourceRepository.create(new Resource(null, "Recurso A"));

        mockMvc.perform(patch("/api/requests/{id}/assign-resource", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d
                                }
                                """.formatted(createdResource.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.resourceId").value(createdResource.id()));
    }

    @Test
    void assignResourceReturns404WhenRequestNotFound() throws Exception {
        Resource createdResource = resourceRepository.create(new Resource(null, "Recurso A"));

        mockMvc.perform(patch("/api/requests/{id}/assign-resource", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d
                                }
                                """.formatted(createdResource.id())))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignResourceReturns404WhenResourceNotFound() throws Exception {
        long requestId = createRequest("Req", "desc", "COMIDA");

        mockMvc.perform(patch("/api/requests/{id}/assign-resource", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": 999999
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignResourceReturns400WhenResourceIdIsNull() throws Exception {
        long requestId = createRequest("Req", "desc", "COMIDA");

        mockMvc.perform(patch("/api/requests/{id}/assign-resource", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteRequestReturns204AndRequestIsGone() throws Exception {
        long createdId = createRequest("Para borrar", "desc", "COMIDA");

        mockMvc.perform(delete("/api/requests/{id}", createdId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/requests/{id}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRequestReturns204WhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/requests/{id}", 999999))
                .andExpect(status().isNoContent());
    }
}
