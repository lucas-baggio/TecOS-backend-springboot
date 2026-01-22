package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
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
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveBudgetByTokenUseCase Tests")
class ApproveBudgetByTokenUseCaseTest {

    @Mock
    private PublicLinkRepository publicLinkRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApproveBudgetByTokenUseCase useCase;

    private Company company;
    private WorkOrder workOrder;
    private PublicLink publicLink;
    private Budget budget;
    private Budget otherApprovedBudget;
    private String token;
    private UUID budgetId;

    @BeforeEach
    void setUp() {
        token = "test-token-123";
        budgetId = UUID.randomUUID();
        
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

        otherApprovedBudget = new Budget();
        otherApprovedBudget.setId(UUID.randomUUID());
        otherApprovedBudget.setWorkOrder(workOrder);
        otherApprovedBudget.setStatus(BudgetStatus.APROVADO);
    }

    @Test
    @DisplayName("Deve aprovar orçamento por token com sucesso (sem usuário autenticado)")
    void shouldApproveBudgetByTokenSuccessfullyWithoutUser() {
        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(budgetRepository.findByWorkOrderIdAndStatus(workOrder.getId(), BudgetStatus.APROVADO))
                .thenReturn(Arrays.asList());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget result = useCase.execute(token, budgetId, null);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.APROVADO);
        assertThat(result.getApprovalMethod()).isEqualTo("link");
        assertThat(result.getApprovedBy()).isNull(); // Cliente aprova = null

        verify(budgetRepository).save(budget);
    }

    @Test
    @DisplayName("Deve aprovar orçamento por token com sucesso (com usuário autenticado)")
    void shouldApproveBudgetByTokenSuccessfullyWithUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCompany(company);

        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(budgetRepository.findByWorkOrderIdAndStatus(workOrder.getId(), BudgetStatus.APROVADO))
                .thenReturn(Arrays.asList());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget result = useCase.execute(token, budgetId, user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.APROVADO);
        assertThat(result.getApprovalMethod()).isEqualTo("link");
        assertThat(result.getApprovedBy()).isNotNull(); // Operador aprova = ID salvo

        verify(userRepository).findById(user.getId());
        verify(budgetRepository).save(budget);
    }

    @Test
    @DisplayName("Deve desaprovar outros orçamentos aprovados (RB-05)")
    void shouldUnapproveOtherApprovedBudgets() {
        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(budgetRepository.findByWorkOrderIdAndStatus(workOrder.getId(), BudgetStatus.APROVADO))
                .thenReturn(Arrays.asList(otherApprovedBudget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(token, budgetId, null);

        // Verificar que o outro orçamento foi desaprovado
        verify(budgetRepository, atLeast(2)).save(any(Budget.class));
        verify(budgetRepository).save(argThat(b -> 
            b.getId().equals(otherApprovedBudget.getId()) && 
            b.getStatus() == BudgetStatus.PENDENTE
        ));
    }

    @Test
    @DisplayName("Deve lançar exceção quando token não encontrado")
    void shouldThrowExceptionWhenTokenNotFound() {
        String nonExistentToken = "non-existent-token";
        when(publicLinkRepository.findByToken(nonExistentToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentToken, budgetId, null))
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

        assertThatThrownBy(() -> useCase.execute(token, budgetId, null))
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

        assertThatThrownBy(() -> useCase.execute(token, budgetId, null))
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

        assertThatThrownBy(() -> useCase.execute(token, budgetId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não está aguardando aprovação");

        verify(budgetRepository, never()).save(any(Budget.class));
    }
}
