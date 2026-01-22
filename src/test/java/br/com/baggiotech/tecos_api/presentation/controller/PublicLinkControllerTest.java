package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.publiclink.*;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.presentation.dto.publiclink.PublicLinkRequest;
import br.com.baggiotech.tecos_api.presentation.dto.publiclink.PublicLinkResponse;
import br.com.baggiotech.tecos_api.presentation.exception.GlobalExceptionHandler;
import br.com.baggiotech.tecos_api.presentation.mapper.publiclink.PublicLinkMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublicLinkController Integration Tests")
class PublicLinkControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ListPublicLinksUseCase listPublicLinksUseCase;

    @Mock
    private CreatePublicLinkUseCase createPublicLinkUseCase;

    @Mock
    private GetPublicLinkByIdUseCase getPublicLinkByIdUseCase;

    @Mock
    private DeletePublicLinkUseCase deletePublicLinkUseCase;

    @Mock
    private PublicLinkMapper mapper;

    private PublicLinkController controller;

    private PublicLink publicLink;
    private WorkOrder workOrder;
    private UUID linkId;
    private UUID workOrderId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        controller = new PublicLinkController(
                listPublicLinksUseCase,
                createPublicLinkUseCase,
                getPublicLinkByIdUseCase,
                deletePublicLinkUseCase,
                mapper
        );

        org.springframework.validation.beanvalidation.LocalValidatorFactoryBean validator =
            new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        linkId = UUID.randomUUID();
        workOrderId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        workOrder = new WorkOrder();
        workOrder.setId(workOrderId);

        publicLink = new PublicLink();
        publicLink.setId(linkId);
        publicLink.setWorkOrder(workOrder);
        publicLink.setToken("test-token-123");
        publicLink.setCreatedAt(LocalDateTime.now());
        publicLink.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/public-links - Deve listar links públicos com sucesso")
    void shouldListPublicLinksSuccessfully() throws Exception {
        List<PublicLink> links = Arrays.asList(publicLink);

        when(listPublicLinksUseCase.execute(workOrderId)).thenReturn(links);
        when(mapper.toResponse(any(PublicLink.class))).thenAnswer(invocation -> {
            PublicLink link = invocation.getArgument(0);
            return new PublicLinkResponse(
                    link.getId(),
                    link.getWorkOrder() != null ? link.getWorkOrder().getId() : null,
                    link.getToken(),
                    "http://localhost:8080/api/public/" + link.getToken(),
                    link.getCreatedAt(),
                    link.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/public-links")
                        .param("workOrderId", workOrderId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].token").value("test-token-123"));
    }

    @Test
    @DisplayName("POST /api/public-links - Deve criar link público com sucesso")
    void shouldCreatePublicLinkSuccessfully() throws Exception {
        PublicLinkRequest request = new PublicLinkRequest(workOrderId);

        when(createPublicLinkUseCase.execute(workOrderId, companyId)).thenReturn(publicLink);
        when(mapper.toResponse(any(PublicLink.class))).thenAnswer(invocation -> {
            PublicLink link = invocation.getArgument(0);
            return new PublicLinkResponse(
                    link.getId(),
                    link.getWorkOrder() != null ? link.getWorkOrder().getId() : null,
                    link.getToken(),
                    "http://localhost:8080/api/public/" + link.getToken(),
                    link.getCreatedAt(),
                    link.getUpdatedAt()
            );
        });

        mockMvc.perform(post("/api/public-links")
                        .param("companyId", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-token-123"));
    }

    @Test
    @DisplayName("GET /api/public-links/{id} - Deve buscar link público por ID")
    void shouldGetPublicLinkById() throws Exception {
        when(getPublicLinkByIdUseCase.execute(linkId)).thenReturn(publicLink);
        when(mapper.toResponse(any(PublicLink.class))).thenAnswer(invocation -> {
            PublicLink link = invocation.getArgument(0);
            return new PublicLinkResponse(
                    link.getId(),
                    link.getWorkOrder() != null ? link.getWorkOrder().getId() : null,
                    link.getToken(),
                    "http://localhost:8080/api/public/" + link.getToken(),
                    link.getCreatedAt(),
                    link.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/public-links/{id}", linkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(linkId.toString()))
                .andExpect(jsonPath("$.token").value("test-token-123"));
    }

    @Test
    @DisplayName("DELETE /api/public-links/{id} - Deve deletar link público com sucesso")
    void shouldDeletePublicLinkSuccessfully() throws Exception {
        doNothing().when(deletePublicLinkUseCase).execute(linkId, companyId);

        mockMvc.perform(delete("/api/public-links/{id}", linkId)
                        .param("companyId", companyId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Link público excluído com sucesso."));

        verify(deletePublicLinkUseCase).execute(linkId, companyId);
    }

    @Test
    @DisplayName("GET /api/public-links/{id} - Deve retornar 404 quando link não encontrado")
    void shouldReturn404WhenLinkNotFound() throws Exception {
        when(getPublicLinkByIdUseCase.execute(linkId))
                .thenThrow(new EntityNotFoundException("PublicLink", linkId));

        mockMvc.perform(get("/api/public-links/{id}", linkId))
                .andExpect(status().isNotFound());
    }
}
