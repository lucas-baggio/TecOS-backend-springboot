package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListBudgetsUseCase Tests")
class ListBudgetsUseCaseTest {

    @Mock
    private BudgetRepository repository;

    @InjectMocks
    private ListBudgetsUseCase useCase;

    private Budget budget1;
    private Budget budget2;
    private UUID companyId;
    private UUID workOrderId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        workOrderId = UUID.randomUUID();

        budget1 = new Budget();
        budget1.setId(UUID.randomUUID());
        budget1.setStatus(BudgetStatus.PENDENTE);
        budget1.setTotalValue(new BigDecimal("100.00"));

        budget2 = new Budget();
        budget2.setId(UUID.randomUUID());
        budget2.setStatus(BudgetStatus.APROVADO);
        budget2.setTotalValue(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Deve listar todos os orçamentos")
    void shouldListAllBudgets() {
        List<Budget> budgets = Arrays.asList(budget1, budget2);
        when(repository.findAll()).thenReturn(budgets);

        Page<Budget> result = useCase.execute(null, null, null, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve filtrar por workOrderId")
    void shouldFilterByWorkOrderId() {
        List<Budget> budgets = Arrays.asList(budget1);
        when(repository.findByWorkOrderId(workOrderId)).thenReturn(budgets);

        Page<Budget> result = useCase.execute(null, workOrderId, null, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByWorkOrderId(workOrderId);
    }

    @Test
    @DisplayName("Deve filtrar por status")
    void shouldFilterByStatus() {
        List<Budget> budgets = Arrays.asList(budget1);
        when(repository.findByStatus(BudgetStatus.PENDENTE)).thenReturn(budgets);

        Page<Budget> result = useCase.execute(null, null, BudgetStatus.PENDENTE, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByStatus(BudgetStatus.PENDENTE);
    }

    @Test
    @DisplayName("Deve filtrar por companyId")
    void shouldFilterByCompanyId() {
        List<Budget> budgets = Arrays.asList(budget1, budget2);
        when(repository.findByCompanyId(companyId)).thenReturn(budgets);

        Page<Budget> result = useCase.execute(companyId, null, null, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findByCompanyId(companyId);
    }

    @Test
    @DisplayName("Deve filtrar por workOrderId e status")
    void shouldFilterByWorkOrderIdAndStatus() {
        List<Budget> budgets = Arrays.asList(budget1);
        when(repository.findByWorkOrderIdAndStatus(workOrderId, BudgetStatus.PENDENTE)).thenReturn(budgets);

        Page<Budget> result = useCase.execute(null, workOrderId, BudgetStatus.PENDENTE, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByWorkOrderIdAndStatus(workOrderId, BudgetStatus.PENDENTE);
    }

    @Test
    @DisplayName("Deve filtrar por companyId e status")
    void shouldFilterByCompanyIdAndStatus() {
        List<Budget> budgets = Arrays.asList(budget1);
        when(repository.findByCompanyIdAndStatus(companyId, BudgetStatus.PENDENTE)).thenReturn(budgets);

        Page<Budget> result = useCase.execute(companyId, null, BudgetStatus.PENDENTE, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByCompanyIdAndStatus(companyId, BudgetStatus.PENDENTE);
    }

    @Test
    @DisplayName("Deve paginar resultados corretamente")
    void shouldPaginateResultsCorrectly() {
        List<Budget> budgets = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Budget budget = new Budget();
            budget.setId(UUID.randomUUID());
            budgets.add(budget);
        }
        when(repository.findAll()).thenReturn(budgets);

        Page<Budget> result = useCase.execute(null, null, null, "created_at", "desc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
    }

    @Test
    @DisplayName("Deve ordenar por total_value")
    void shouldSortByTotalValue() {
        List<Budget> budgets = Arrays.asList(budget2, budget1); // Ordem inversa
        when(repository.findAll()).thenReturn(budgets);

        Page<Budget> result = useCase.execute(null, null, null, "total_value", "asc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll();
    }
}
