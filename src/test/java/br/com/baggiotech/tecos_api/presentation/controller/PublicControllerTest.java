package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.publiclink.ApproveBudgetByTokenUseCase;
import br.com.baggiotech.tecos_api.application.publiclink.GetPublicWorkOrderByTokenUseCase;
import br.com.baggiotech.tecos_api.application.publiclink.RejectBudgetByTokenUseCase;
import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.presentation.controller.PublicController;
import br.com.baggiotech.tecos_api.presentation.dto.publiclink.RejectBudgetByTokenRequest;
import br.com.baggiotech.tecos_api.presentation.exception.GlobalExceptionHandler;
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
@DisplayName("PublicController Integration Tests")
class PublicControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GetPublicWorkOrderByTokenUseCase getPublicWorkOrderByTokenUseCase;

    @Mock
    private ApproveBudgetByTokenUseCase approveBudgetByTokenUseCase;

    @Mock
    private RejectBudgetByTokenUseCase rejectBudgetByTokenUseCase;

    private PublicController controller;

    private WorkOrder workOrder;
    private Client client;
    private Equipment equipment;
    private Budget budget;
    private String token;
    private UUID budgetId;

    @BeforeEach
    void setUp() {
        controller = new PublicController(
                getPublicWorkOrderByTokenUseCase,
                approveBudgetByTokenUseCase,
                rejectBudgetByTokenUseCase
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

        token = "test-token-123";
        budgetId = UUID.randomUUID();

        Company company = new Company();
        company.setId(UUID.randomUUID());

        client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Test Client");
        client.setPhone("11999999999");
        client.setEmail("client@example.com");

        equipment = new Equipment();
        equipment.setId(UUID.randomUUID());
        equipment.setType("Notebook");
        equipment.setBrand("Dell");
        equipment.setModel("Inspiron");
        equipment.setSerialNumber("SN123456");

        workOrder = new WorkOrder();
        workOrder.setId(UUID.randomUUID());
        workOrder.setCompany(company);
        workOrder.setClient(client);
        workOrder.setEquipment(equipment);
        workOrder.setStatus(OrderStatus.AGUARDANDO_APROVACAO);
        workOrder.setReportedDefect("Test defect");
        workOrder.setReturnOrder(false);
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());

        budget = new Budget();
        budget.setId(budgetId);
        budget.setServiceValue(new BigDecimal("100.00"));
        budget.setPartsValue(new BigDecimal("50.00"));
        budget.setTotalValue(new BigDecimal("150.00"));
        budget.setStatus(BudgetStatus.PENDENTE);
        budget.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/public/{token} - Deve retornar informações da OS (rota pública)")
    void shouldGetWorkOrderByToken() throws Exception {
        GetPublicWorkOrderByTokenUseCase.PublicWorkOrderData data = 
            new GetPublicWorkOrderByTokenUseCase.PublicWorkOrderData(workOrder, Arrays.asList(budget));

        when(getPublicWorkOrderByTokenUseCase.execute(token)).thenReturn(data);

        mockMvc.perform(get("/api/public/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workOrder.id").exists())
                .andExpect(jsonPath("$.workOrder.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.client.name").value("Test Client"))
                .andExpect(jsonPath("$.equipment.type").value("Notebook"))
                .andExpect(jsonPath("$.budgets").isArray())
                .andExpect(jsonPath("$.budgets[0].totalValue").value(150.00));
    }

    @Test
    @DisplayName("POST /api/public/{token}/budgets/{budgetId}/approve - Deve aprovar orçamento (rota pública)")
    void shouldApproveBudgetByToken() throws Exception {
        budget.setStatus(BudgetStatus.APROVADO);
        budget.setApprovedAt(LocalDateTime.now());

        when(approveBudgetByTokenUseCase.execute(token, budgetId, null)).thenReturn(budget);

        mockMvc.perform(post("/api/public/{token}/budgets/{budgetId}/approve", token, budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Orçamento aprovado com sucesso!"))
                .andExpect(jsonPath("$.budget.status").value("APROVADO"));
    }

    @Test
    @DisplayName("POST /api/public/{token}/budgets/{budgetId}/reject - Deve rejeitar orçamento (rota pública)")
    void shouldRejectBudgetByToken() throws Exception {
        budget.setStatus(BudgetStatus.REJEITADO);
        budget.setRejectionReason("Motivo de rejeição válido com mais de 10 caracteres");
        RejectBudgetByTokenRequest request = new RejectBudgetByTokenRequest(
                "Motivo de rejeição válido com mais de 10 caracteres"
        );

        when(rejectBudgetByTokenUseCase.execute(token, budgetId, request.rejectionReason())).thenReturn(budget);

        mockMvc.perform(post("/api/public/{token}/budgets/{budgetId}/reject", token, budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Orçamento rejeitado com sucesso!"))
                .andExpect(jsonPath("$.budget.status").value("REJEITADO"))
                .andExpect(jsonPath("$.budget.rejectionReason").exists());
    }

    @Test
    @DisplayName("GET /api/public/{token} - Deve retornar 404 quando token não encontrado")
    void shouldReturn404WhenTokenNotFound() throws Exception {
        String nonExistentToken = "non-existent-token";
        when(getPublicWorkOrderByTokenUseCase.execute(nonExistentToken))
                .thenThrow(new EntityNotFoundException("PublicLink", "Token não encontrado"));

        mockMvc.perform(get("/api/public/{token}", nonExistentToken))
                .andExpect(status().isNotFound());
    }
}
