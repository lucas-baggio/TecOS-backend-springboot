package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejectBudgetByTokenUseCase Tests")
class RejectBudgetByTokenUseCaseTest {

    @Mock
    private PublicLinkRepository publicLinkRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private RejectBudgetByTokenUseCase useCase;

    private Company company;
    private WorkOrder workOrder;
    private PublicLink publicLink;
    private Budget budget;
    private String token;
    private UUID budgetId;
    private String rejectionReason;

    @BeforeEach
    void setUp() {
        token = "test-token-123";
        budgetId = UUID.randomUUID();
        rejectionReason = "Este é um motivo de rejeição válido com mais de 10 caracteres";
        
        company = new Company();
        company.setId(UUID.randomUUID());

        workOrder = new WorkOrder();
        workOrder.setId(UUID.randomUUID());
        workOrder.setCompany(company);
        workOrder.setStatus(OrderStatus.AGUARDANDO_APROVACAO);

        publicLink = new PublicLink();
        publicLink.setId(UUID.randomUUID());
        publicLink.setWorkOrder(workOrder);
        publicLink.setToken(token);

        budget = new Budget();
        budget.setId(budgetId);
        budget.setWorkOrder(workOrder);
        budget.setStatus(BudgetStatus.PENDENTE);
        budget.setTotalValue(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve rejeitar orçamento por token com sucesso")
    void shouldRejectBudgetByTokenSuccessfully() {
        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget result = useCase.execute(token, budgetId, rejectionReason);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.REJEITADO);
        assertThat(result.getRejectionReason()).isEqualTo(rejectionReason.trim());

        verify(budgetRepository).save(budget);
    }

    @Test
    @DisplayName("Deve lançar exceção quando token não encontrado")
    void shouldThrowExceptionWhenTokenNotFound() {
        String nonExistentToken = "non-existent-token";
        when(publicLinkRepository.findByToken(nonExistentToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentToken, budgetId, rejectionReason))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("PublicLink");

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não pertence à work order do link")
    void shouldThrowExceptionWhenBudgetDoesNotBelongToWorkOrder() {
        WorkOrder otherWorkOrder = new WorkOrder();
        otherWorkOrder.setId(UUID.randomUUID());
        budget.setWorkOrder(otherWorkOrder);

        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(token, budgetId, rejectionReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pertence à ordem de serviço");

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento já foi processado")
    void shouldThrowExceptionWhenBudgetAlreadyProcessed() {
        budget.setStatus(BudgetStatus.APROVADO);

        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(token, budgetId, rejectionReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já foi processado");

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando OS não está aguardando aprovação")
    void shouldThrowExceptionWhenWorkOrderNotWaitingApproval() {
        workOrder.setStatus(OrderStatus.EM_CONSERTO);

        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(token, budgetId, rejectionReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não está aguardando aprovação");

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando rejection_reason é muito curto")
    void shouldThrowExceptionWhenRejectionReasonIsTooShort() {
        String shortReason = "Curto";

        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(token, budgetId, shortReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínimo 10 caracteres");

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve trim rejection_reason")
    void shouldTrimRejectionReason() {
        String reasonWithSpaces = "   Motivo válido com espaços   ";
        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget saved = invocation.getArgument(0);
            assertThat(saved.getRejectionReason()).isEqualTo("Motivo válido com espaços");
            return saved;
        });

        useCase.execute(token, budgetId, reasonWithSpaces);

        verify(budgetRepository).save(any(Budget.class));
    }
}
