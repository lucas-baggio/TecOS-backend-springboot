package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetBudgetByIdUseCase Tests")
class GetBudgetByIdUseCaseTest {

    @Mock
    private BudgetRepository repository;

    @InjectMocks
    private GetBudgetByIdUseCase useCase;

    private Budget budget;
    private UUID budgetId;

    @BeforeEach
    void setUp() {
        budgetId = UUID.randomUUID();
        budget = new Budget();
        budget.setId(budgetId);
        budget.setServiceValue(new BigDecimal("100.00"));
        budget.setPartsValue(new BigDecimal("50.00"));
        budget.setTotalValue(new BigDecimal("150.00"));
        budget.setStatus(BudgetStatus.PENDENTE);
    }

    @Test
    @DisplayName("Deve buscar orçamento por ID com sucesso")
    void shouldGetBudgetByIdSuccessfully() {
        when(repository.findById(budgetId)).thenReturn(Optional.of(budget));

        Budget result = useCase.execute(budgetId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(budgetId);
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.PENDENTE);
        verify(repository).findById(budgetId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não encontrado")
    void shouldThrowExceptionWhenBudgetNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Budget");

        verify(repository).findById(nonExistentId);
    }
}
