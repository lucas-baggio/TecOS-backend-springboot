package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.budget.*;
import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.presentation.dto.budget.ApproveBudgetRequest;
import br.com.baggiotech.tecos_api.presentation.dto.budget.BudgetRequest;
import br.com.baggiotech.tecos_api.presentation.dto.budget.BudgetResponse;
import br.com.baggiotech.tecos_api.presentation.dto.budget.RejectBudgetRequest;
import br.com.baggiotech.tecos_api.presentation.exception.GlobalExceptionHandler;
import br.com.baggiotech.tecos_api.presentation.mapper.budget.BudgetMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetController Integration Tests")
class BudgetControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ListBudgetsUseCase listBudgetsUseCase;

    @Mock
    private CreateBudgetUseCase createBudgetUseCase;

    @Mock
    private GetBudgetByIdUseCase getBudgetByIdUseCase;

    @Mock
    private ApproveBudgetUseCase approveBudgetUseCase;

    @Mock
    private RejectBudgetUseCase rejectBudgetUseCase;

    @Mock
    private BudgetMapper mapper;

    private BudgetController controller;

    private Budget budget;
    private Company company;
    private WorkOrder workOrder;
    private User creator;
    private UUID budgetId;
    private UUID companyId;
    private UUID workOrderId;

    @BeforeEach
    void setUp() {
        controller = new BudgetController(
                listBudgetsUseCase,
                createBudgetUseCase,
                getBudgetByIdUseCase,
                approveBudgetUseCase,
                rejectBudgetUseCase,
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

        companyId = UUID.randomUUID();
        workOrderId = UUID.randomUUID();
        budgetId = UUID.randomUUID();

        company = new Company();
        company.setId(companyId);
        company.setName("Test Company");

        workOrder = new WorkOrder();
        workOrder.setId(workOrderId);

        creator = new User();
        creator.setId(UUID.randomUUID());
        creator.setName("Creator User");

        budget = new Budget();
        budget.setId(budgetId);
        budget.setCompany(company);
        budget.setWorkOrder(workOrder);
        budget.setServiceValue(new BigDecimal("100.00"));
        budget.setPartsValue(new BigDecimal("50.00"));
        budget.setTotalValue(new BigDecimal("150.00"));
        budget.setStatus(BudgetStatus.PENDENTE);
        budget.setCreatedBy(creator);
        budget.setCreatedAt(LocalDateTime.now());
        budget.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/budgets - Deve listar orçamentos com sucesso")
    void shouldListBudgetsSuccessfully() throws Exception {
        List<Budget> budgets = Arrays.asList(budget);
        Page<Budget> page = new PageImpl<>(budgets, PageRequest.of(0, 15), 1);

        when(listBudgetsUseCase.execute(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);
        when(mapper.toResponse(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            return new BudgetResponse(
                    b.getId(),
                    b.getCompany() != null ? b.getCompany().getId() : null,
                    b.getCompany() != null ? b.getCompany().getName() : null,
                    b.getWorkOrder() != null ? b.getWorkOrder().getId() : null,
                    b.getStatus(),
                    b.getServiceValue(),
                    b.getPartsValue(),
                    b.getTotalValue(),
                    b.getRejectionReason(),
                    b.getCreatedBy() != null ? b.getCreatedBy().getId() : null,
                    b.getCreatedBy() != null ? b.getCreatedBy().getName() : null,
                    b.getApprovedAt(),
                    b.getApprovalMethod(),
                    b.getApprovedBy() != null ? b.getApprovedBy().getId() : null,
                    b.getApprovedBy() != null ? b.getApprovedBy().getName() : null,
                    b.getCreatedAt(),
                    b.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/budgets")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/budgets - Deve filtrar por workOrderId")
    void shouldFilterByWorkOrderId() throws Exception {
        List<Budget> budgets = Arrays.asList(budget);
        Page<Budget> page = new PageImpl<>(budgets, PageRequest.of(0, 15), 1);

        when(listBudgetsUseCase.execute(any(), eq(workOrderId), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);
        when(mapper.toResponse(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            return new BudgetResponse(
                    b.getId(), b.getCompany() != null ? b.getCompany().getId() : null,
                    b.getCompany() != null ? b.getCompany().getName() : null,
                    b.getWorkOrder() != null ? b.getWorkOrder().getId() : null,
                    b.getStatus(), b.getServiceValue(), b.getPartsValue(), b.getTotalValue(),
                    b.getRejectionReason(),
                    b.getCreatedBy() != null ? b.getCreatedBy().getId() : null,
                    b.getCreatedBy() != null ? b.getCreatedBy().getName() : null,
                    b.getApprovedAt(), b.getApprovalMethod(),
                    b.getApprovedBy() != null ? b.getApprovedBy().getId() : null,
                    b.getApprovedBy() != null ? b.getApprovedBy().getName() : null,
                    b.getCreatedAt(), b.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/budgets")
                        .param("workOrderId", workOrderId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/budgets - Deve criar orçamento com sucesso")
    void shouldCreateBudgetSuccessfully() throws Exception {
        BudgetRequest request = new BudgetRequest(
                companyId,
                workOrderId,
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                creator.getId()
        );

        when(createBudgetUseCase.execute(any(), any(), any(), any(), any(), any()))
                .thenReturn(budget);
        when(mapper.toResponse(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            return new BudgetResponse(
                    b.getId(), b.getCompany() != null ? b.getCompany().getId() : null,
                    b.getCompany() != null ? b.getCompany().getName() : null,
                    b.getWorkOrder() != null ? b.getWorkOrder().getId() : null,
                    b.getStatus(), b.getServiceValue(), b.getPartsValue(), b.getTotalValue(),
                    b.getRejectionReason(),
                    b.getCreatedBy() != null ? b.getCreatedBy().getId() : null,
                    b.getCreatedBy() != null ? b.getCreatedBy().getName() : null,
                    b.getApprovedAt(), b.getApprovalMethod(),
                    b.getApprovedBy() != null ? b.getApprovedBy().getId() : null,
                    b.getApprovedBy() != null ? b.getApprovedBy().getName() : null,
                    b.getCreatedAt(), b.getUpdatedAt()
            );
        });

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @DisplayName("GET /api/budgets/{id} - Deve buscar orçamento por ID")
    void shouldGetBudgetById() throws Exception {
        when(getBudgetByIdUseCase.execute(budgetId)).thenReturn(budget);
        when(mapper.toResponse(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            return new BudgetResponse(
                    b.getId(), b.getCompany() != null ? b.getCompany().getId() : null,
                    b.getCompany() != null ? b.getCompany().getName() : null,
                    b.getWorkOrder() != null ? b.getWorkOrder().getId() : null,
                    b.getStatus(), b.getServiceValue(), b.getPartsValue(), b.getTotalValue(),
                    b.getRejectionReason(),
                    b.getCreatedBy() != null ? b.getCreatedBy().getId() : null,
                    b.getCreatedBy() != null ? b.getCreatedBy().getName() : null,
                    b.getApprovedAt(), b.getApprovalMethod(),
                    b.getApprovedBy() != null ? b.getApprovedBy().getId() : null,
                    b.getApprovedBy() != null ? b.getApprovedBy().getName() : null,
                    b.getCreatedAt(), b.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/budgets/{id}", budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @DisplayName("PUT /api/budgets/{id} - Deve retornar erro (orçamento não é editável)")
    void shouldReturnErrorWhenTryingToUpdate() throws Exception {
        BudgetRequest request = new BudgetRequest(
                companyId,
                workOrderId,
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("300.00"),
                creator.getId()
        );

        mockMvc.perform(put("/api/budgets/{id}", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/budgets/{id} - Deve retornar erro (orçamento não pode ser excluído)")
    void shouldReturnErrorWhenTryingToDelete() throws Exception {
        mockMvc.perform(delete("/api/budgets/{id}", budgetId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/budgets/{id}/approve - Deve aprovar orçamento com sucesso")
    void shouldApproveBudgetSuccessfully() throws Exception {
        budget.setStatus(BudgetStatus.APROVADO);
        ApproveBudgetRequest request = new ApproveBudgetRequest("presential", UUID.randomUUID());

        when(approveBudgetUseCase.execute(any(), eq(companyId), any(), any()))
                .thenReturn(budget);
        when(mapper.toResponse(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            return new BudgetResponse(
                    b.getId(), b.getCompany() != null ? b.getCompany().getId() : null,
                    b.getCompany() != null ? b.getCompany().getName() : null,
                    b.getWorkOrder() != null ? b.getWorkOrder().getId() : null,
                    b.getStatus(), b.getServiceValue(), b.getPartsValue(), b.getTotalValue(),
                    b.getRejectionReason(),
                    b.getCreatedBy() != null ? b.getCreatedBy().getId() : null,
                    b.getCreatedBy() != null ? b.getCreatedBy().getName() : null,
                    b.getApprovedAt(), b.getApprovalMethod(),
                    b.getApprovedBy() != null ? b.getApprovedBy().getId() : null,
                    b.getApprovedBy() != null ? b.getApprovedBy().getName() : null,
                    b.getCreatedAt(), b.getUpdatedAt()
            );
        });

        mockMvc.perform(post("/api/budgets/{id}/approve", budgetId)
                        .param("companyId", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    @DisplayName("POST /api/budgets/{id}/reject - Deve rejeitar orçamento com sucesso")
    void shouldRejectBudgetSuccessfully() throws Exception {
        budget.setStatus(BudgetStatus.REJEITADO);
        budget.setRejectionReason("Motivo de rejeição válido com mais de 10 caracteres");
        RejectBudgetRequest request = new RejectBudgetRequest("Motivo de rejeição válido com mais de 10 caracteres");

        when(rejectBudgetUseCase.execute(any(), eq(companyId), any()))
                .thenReturn(budget);
        when(mapper.toResponse(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            return new BudgetResponse(
                    b.getId(), b.getCompany() != null ? b.getCompany().getId() : null,
                    b.getCompany() != null ? b.getCompany().getName() : null,
                    b.getWorkOrder() != null ? b.getWorkOrder().getId() : null,
                    b.getStatus(), b.getServiceValue(), b.getPartsValue(), b.getTotalValue(),
                    b.getRejectionReason(),
                    b.getCreatedBy() != null ? b.getCreatedBy().getId() : null,
                    b.getCreatedBy() != null ? b.getCreatedBy().getName() : null,
                    b.getApprovedAt(), b.getApprovalMethod(),
                    b.getApprovedBy() != null ? b.getApprovedBy().getId() : null,
                    b.getApprovedBy() != null ? b.getApprovedBy().getName() : null,
                    b.getCreatedAt(), b.getUpdatedAt()
            );
        });

        mockMvc.perform(post("/api/budgets/{id}/reject", budgetId)
                        .param("companyId", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADO"))
                .andExpect(jsonPath("$.rejectionReason").exists());
    }

    @Test
    @DisplayName("GET /api/budgets/{id} - Deve retornar 404 quando orçamento não encontrado")
    void shouldReturn404WhenBudgetNotFound() throws Exception {
        when(getBudgetByIdUseCase.execute(budgetId))
                .thenThrow(new EntityNotFoundException("Budget", budgetId));

        mockMvc.perform(get("/api/budgets/{id}", budgetId))
                .andExpect(status().isNotFound());
    }
}
