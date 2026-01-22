package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveBudgetUseCase Tests")
class ApproveBudgetUseCaseTest {

    @Mock
    private BudgetRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private ApproveBudgetUseCase useCase;

    private Company company;
    private User approvedBy;
    private WorkOrder workOrder;
    private Budget budget;
    private Budget otherApprovedBudget;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());

        approvedBy = new User();
        approvedBy.setId(UUID.randomUUID());
        approvedBy.setCompany(company);

        workOrder = new WorkOrder();
        workOrder.setId(UUID.randomUUID());

        budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setCompany(company);
        budget.setWorkOrder(workOrder);
        budget.setStatus(BudgetStatus.PENDENTE);

        otherApprovedBudget = new Budget();
        otherApprovedBudget.setId(UUID.randomUUID());
        otherApprovedBudget.setCompany(company);
        otherApprovedBudget.setWorkOrder(workOrder);
        otherApprovedBudget.setStatus(BudgetStatus.APROVADO);
    }

    @Test
    @DisplayName("Deve aprovar orçamento com sucesso (método presential)")
    void shouldApproveBudgetSuccessfullyPresential() {
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(repository.findByWorkOrderIdAndStatus(workOrder.getId(), BudgetStatus.APROVADO))
                .thenReturn(Arrays.asList());
        when(repository.save(any(Budget.class))).thenReturn(budget);

        Budget result = useCase.execute(budget.getId(), company.getId(), "presential", approvedBy.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.APROVADO);
        assertThat(result.getApprovalMethod()).isEqualTo("presential");
        assertThat(result.getApprovedAt()).isNotNull();
        assertThat(result.getApprovedBy()).isNull(); // Presential não salva approvedBy

        verify(repository).save(budget);
        verify(metrics).incrementBudgetsUpdated();
    }

    @Test
    @DisplayName("Deve aprovar orçamento com sucesso (método link)")
    void shouldApproveBudgetSuccessfullyLink() {
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(repository.findByWorkOrderIdAndStatus(workOrder.getId(), BudgetStatus.APROVADO))
                .thenReturn(Arrays.asList());
        when(userRepository.findById(approvedBy.getId())).thenReturn(Optional.of(approvedBy));
        when(repository.save(any(Budget.class))).thenReturn(budget);

        Budget result = useCase.execute(budget.getId(), company.getId(), "link", approvedBy.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.APROVADO);
        assertThat(result.getApprovalMethod()).isEqualTo("link");
        assertThat(result.getApprovedAt()).isNotNull();
        assertThat(result.getApprovedBy()).isNotNull();

        verify(userRepository).findById(approvedBy.getId());
        verify(repository).save(budget);
        verify(metrics).incrementBudgetsUpdated();
    }

    @Test
    @DisplayName("Deve desaprovar outros orçamentos aprovados da mesma OS (RB-05)")
    void shouldUnapproveOtherApprovedBudgetsFromSameWorkOrder() {
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(repository.findByWorkOrderIdAndStatus(workOrder.getId(), BudgetStatus.APROVADO))
                .thenReturn(Arrays.asList(otherApprovedBudget));
        when(repository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(budget.getId(), company.getId(), "presential", approvedBy.getId());

        // Verificar que o outro orçamento foi desaprovado
        verify(repository, atLeast(2)).save(any(Budget.class));
        verify(repository).save(argThat(b -> 
            b.getId().equals(otherApprovedBudget.getId()) && 
            b.getStatus() == BudgetStatus.PENDENTE
        ));
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não encontrado")
    void shouldThrowExceptionWhenBudgetNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId, company.getId(), "presential", approvedBy.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Budget");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não pertence à company")
    void shouldThrowExceptionWhenBudgetDoesNotBelongToCompany() {
        Company otherCompany = new Company();
        otherCompany.setId(UUID.randomUUID());
        budget.setCompany(otherCompany);

        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(budget.getId(), company.getId(), "presential", approvedBy.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não está PENDENTE")
    void shouldThrowExceptionWhenBudgetIsNotPending() {
        budget.setStatus(BudgetStatus.APROVADO);

        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(budget.getId(), company.getId(), "presential", approvedBy.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pendentes");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando approval_method é inválido")
    void shouldThrowExceptionWhenApprovalMethodIsInvalid() {
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(budget.getId(), company.getId(), "invalid", approvedBy.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Método de aprovação");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário aprovador não existe (método link)")
    void shouldThrowExceptionWhenApproverNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(budget.getId(), company.getId(), "link", nonExistentUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User");

        verify(repository, never()).save(any(Budget.class));
    }
}
