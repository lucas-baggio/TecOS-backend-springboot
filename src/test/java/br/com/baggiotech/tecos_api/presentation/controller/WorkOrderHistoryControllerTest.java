package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.workorderhistory.*;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.presentation.dto.workorderhistory.WorkOrderHistoryResponse;
import br.com.baggiotech.tecos_api.presentation.exception.GlobalExceptionHandler;
import br.com.baggiotech.tecos_api.presentation.mapper.workorderhistory.WorkOrderHistoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
@DisplayName("WorkOrderHistoryController Integration Tests")
class WorkOrderHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ListWorkOrderHistoriesUseCase listWorkOrderHistoriesUseCase;

    @Mock
    private GetWorkOrderHistoryByIdUseCase getWorkOrderHistoryByIdUseCase;

    @Mock
    private WorkOrderHistoryMapper mapper;

    private WorkOrderHistoryController controller;

    private WorkOrderHistory history;
    private UUID historyId;
    private UUID workOrderId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        controller = new WorkOrderHistoryController(
                listWorkOrderHistoriesUseCase,
                getWorkOrderHistoryByIdUseCase,
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

        historyId = UUID.randomUUID();
        workOrderId = UUID.randomUUID();
        userId = UUID.randomUUID();

        history = new WorkOrderHistory();
        history.setId(historyId);
        history.setStatusBefore(OrderStatus.RECEBIDO);
        history.setStatusAfter(OrderStatus.EM_ANALISE);
        history.setObservation("Test observation");
        history.setCreatedAt(LocalDateTime.now());
        history.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/work-order-histories - Deve listar históricos com sucesso")
    void shouldListHistoriesSuccessfully() throws Exception {
        List<WorkOrderHistory> histories = Arrays.asList(history);
        Page<WorkOrderHistory> page = new PageImpl<>(histories, PageRequest.of(0, 15), 1);

        when(listWorkOrderHistoriesUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);
        when(mapper.toResponse(any(WorkOrderHistory.class))).thenAnswer(invocation -> {
            WorkOrderHistory h = invocation.getArgument(0);
            return new WorkOrderHistoryResponse(
                    h.getId(),
                    h.getWorkOrder() != null ? h.getWorkOrder().getId() : null,
                    h.getUser() != null ? h.getUser().getId() : null,
                    h.getUser() != null ? h.getUser().getName() : null,
                    h.getStatusBefore(),
                    h.getStatusAfter(),
                    h.getObservation(),
                    h.getCreatedAt(),
                    h.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/work-order-histories")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].statusAfter").value("EM_ANALISE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/work-order-histories - Deve filtrar por workOrderId")
    void shouldFilterByWorkOrderId() throws Exception {
        List<WorkOrderHistory> histories = Arrays.asList(history);
        Page<WorkOrderHistory> page = new PageImpl<>(histories, PageRequest.of(0, 15), 1);

        when(listWorkOrderHistoriesUseCase.execute(eq(workOrderId), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);
        when(mapper.toResponse(any(WorkOrderHistory.class))).thenAnswer(invocation -> {
            WorkOrderHistory h = invocation.getArgument(0);
            return new WorkOrderHistoryResponse(
                    h.getId(), h.getWorkOrder() != null ? h.getWorkOrder().getId() : null,
                    h.getUser() != null ? h.getUser().getId() : null,
                    h.getUser() != null ? h.getUser().getName() : null,
                    h.getStatusBefore(), h.getStatusAfter(), h.getObservation(),
                    h.getCreatedAt(), h.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/work-order-histories")
                        .param("workOrderId", workOrderId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/work-order-histories - Deve filtrar por userId")
    void shouldFilterByUserId() throws Exception {
        List<WorkOrderHistory> histories = Arrays.asList(history);
        Page<WorkOrderHistory> page = new PageImpl<>(histories, PageRequest.of(0, 15), 1);

        when(listWorkOrderHistoriesUseCase.execute(any(), eq(userId), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);
        when(mapper.toResponse(any(WorkOrderHistory.class))).thenAnswer(invocation -> {
            WorkOrderHistory h = invocation.getArgument(0);
            return new WorkOrderHistoryResponse(
                    h.getId(), h.getWorkOrder() != null ? h.getWorkOrder().getId() : null,
                    h.getUser() != null ? h.getUser().getId() : null,
                    h.getUser() != null ? h.getUser().getName() : null,
                    h.getStatusBefore(), h.getStatusAfter(), h.getObservation(),
                    h.getCreatedAt(), h.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/work-order-histories")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/work-order-histories/{id} - Deve buscar histórico por ID")
    void shouldGetHistoryById() throws Exception {
        when(getWorkOrderHistoryByIdUseCase.execute(historyId)).thenReturn(history);
        when(mapper.toResponse(any(WorkOrderHistory.class))).thenAnswer(invocation -> {
            WorkOrderHistory h = invocation.getArgument(0);
            return new WorkOrderHistoryResponse(
                    h.getId(), h.getWorkOrder() != null ? h.getWorkOrder().getId() : null,
                    h.getUser() != null ? h.getUser().getId() : null,
                    h.getUser() != null ? h.getUser().getName() : null,
                    h.getStatusBefore(), h.getStatusAfter(), h.getObservation(),
                    h.getCreatedAt(), h.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/work-order-histories/{id}", historyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(historyId.toString()))
                .andExpect(jsonPath("$.statusAfter").value("EM_ANALISE"));
    }

    @Test
    @DisplayName("GET /api/work-order-histories/{id} - Deve retornar 404 quando histórico não encontrado")
    void shouldReturn404WhenHistoryNotFound() throws Exception {
        when(getWorkOrderHistoryByIdUseCase.execute(historyId))
                .thenThrow(new EntityNotFoundException("WorkOrderHistory", historyId));

        mockMvc.perform(get("/api/work-order-histories/{id}", historyId))
                .andExpect(status().isNotFound());
    }
}
