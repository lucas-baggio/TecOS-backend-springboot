package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejectBudgetUseCase Tests")
class RejectBudgetUseCaseTest {

    @Mock
    private BudgetRepository repository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private RejectBudgetUseCase useCase;

    private Company company;
    private Budget budget;
    private String rejectionReason;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());

        budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setCompany(company);
        budget.setStatus(BudgetStatus.PENDENTE);

        rejectionReason = "Este é um motivo de rejeição válido com mais de 10 caracteres";
    }

    @Test
    @DisplayName("Deve rejeitar orçamento pendente com sucesso")
    void shouldRejectPendingBudgetSuccessfully() {
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(repository.save(any(Budget.class))).thenReturn(budget);

        Budget result = useCase.execute(budget.getId(), company.getId(), rejectionReason);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.REJEITADO);
        assertThat(result.getRejectionReason()).isEqualTo(rejectionReason.trim());

        verify(repository).save(budget);
        verify(metrics).incrementBudgetsUpdated();
    }

    @Test
    @DisplayName("Deve rejeitar orçamento aprovado com sucesso")
    void shouldRejectApprovedBudgetSuccessfully() {
        budget.setStatus(BudgetStatus.APROVADO);
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(repository.save(any(Budget.class))).thenReturn(budget);

        Budget result = useCase.execute(budget.getId(), company.getId(), rejectionReason);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.REJEITADO);
        assertThat(result.getRejectionReason()).isEqualTo(rejectionReason.trim());

        verify(repository).save(budget);
        verify(metrics).incrementBudgetsUpdated();
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não encontrado")
    void shouldThrowExceptionWhenBudgetNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId, company.getId(), rejectionReason))
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

        assertThatThrownBy(() -> useCase.execute(budget.getId(), company.getId(), rejectionReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento já está rejeitado")
    void shouldThrowExceptionWhenBudgetIsAlreadyRejected() {
        budget.setStatus(BudgetStatus.REJEITADO);
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(budget.getId(), company.getId(), rejectionReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pendentes ou aprovados");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando rejection_reason é muito curto")
    void shouldThrowExceptionWhenRejectionReasonIsTooShort() {
        String shortReason = "Curto";
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(budget.getId(), company.getId(), shortReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínimo 10 caracteres");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando rejection_reason é null")
    void shouldThrowExceptionWhenRejectionReasonIsNull() {
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> useCase.execute(budget.getId(), company.getId(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínimo 10 caracteres");

        verify(repository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve trim rejection_reason")
    void shouldTrimRejectionReason() {
        String reasonWithSpaces = "   Motivo válido com espaços   ";
        when(repository.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(repository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget saved = invocation.getArgument(0);
            assertThat(saved.getRejectionReason()).isEqualTo("Motivo válido com espaços");
            return saved;
        });

        useCase.execute(budget.getId(), company.getId(), reasonWithSpaces);

        verify(repository).save(any(Budget.class));
    }
}
