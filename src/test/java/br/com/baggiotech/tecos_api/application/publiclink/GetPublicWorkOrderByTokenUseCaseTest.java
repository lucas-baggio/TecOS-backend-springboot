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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPublicWorkOrderByTokenUseCase Tests")
class GetPublicWorkOrderByTokenUseCaseTest {

    @Mock
    private PublicLinkRepository publicLinkRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private GetPublicWorkOrderByTokenUseCase useCase;

    private Company company;
    private WorkOrder workOrder;
    private PublicLink publicLink;
    private Budget budget1;
    private Budget budget2;
    private String token;

    @BeforeEach
    void setUp() {
        token = "test-token-123";
        
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

        budget1 = new Budget();
        budget1.setId(UUID.randomUUID());
        budget1.setWorkOrder(workOrder);
        budget1.setServiceValue(new BigDecimal("100.00"));
        budget1.setPartsValue(new BigDecimal("50.00"));
        budget1.setTotalValue(new BigDecimal("150.00"));
        budget1.setStatus(BudgetStatus.PENDENTE);
        budget1.setCreatedAt(LocalDateTime.now().minusHours(2));

        budget2 = new Budget();
        budget2.setId(UUID.randomUUID());
        budget2.setWorkOrder(workOrder);
        budget2.setServiceValue(new BigDecimal("200.00"));
        budget2.setPartsValue(new BigDecimal("100.00"));
        budget2.setTotalValue(new BigDecimal("300.00"));
        budget2.setStatus(BudgetStatus.APROVADO);
        budget2.setCreatedAt(LocalDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("Deve buscar work order por token com sucesso")
    void shouldGetWorkOrderByTokenSuccessfully() {
        List<Budget> budgets = Arrays.asList(budget1, budget2);
        
        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findByWorkOrderId(workOrder.getId())).thenReturn(budgets);

        GetPublicWorkOrderByTokenUseCase.PublicWorkOrderData result = useCase.execute(token);

        assertThat(result).isNotNull();
        assertThat(result.getWorkOrder()).isNotNull();
        assertThat(result.getWorkOrder().getId()).isEqualTo(workOrder.getId());
        assertThat(result.getBudgets()).hasSize(2);
        
        // Verificar que budgets estão ordenados por created_at desc (mais recente primeiro)
        assertThat(result.getBudgets().get(0).getId()).isEqualTo(budget2.getId());
        assertThat(result.getBudgets().get(1).getId()).isEqualTo(budget1.getId());

        verify(publicLinkRepository).findByToken(token);
        verify(budgetRepository).findByWorkOrderId(workOrder.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há orçamentos")
    void shouldReturnEmptyListWhenNoBudgets() {
        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));
        when(budgetRepository.findByWorkOrderId(workOrder.getId())).thenReturn(List.of());

        GetPublicWorkOrderByTokenUseCase.PublicWorkOrderData result = useCase.execute(token);

        assertThat(result).isNotNull();
        assertThat(result.getBudgets()).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar exceção quando token não encontrado")
    void shouldThrowExceptionWhenTokenNotFound() {
        String nonExistentToken = "non-existent-token";
        when(publicLinkRepository.findByToken(nonExistentToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentToken))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("PublicLink");

        verify(budgetRepository, never()).findByWorkOrderId(any(UUID.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order não encontrado no link")
    void shouldThrowExceptionWhenWorkOrderNotFound() {
        publicLink.setWorkOrder(null);
        
        when(publicLinkRepository.findByToken(token)).thenReturn(Optional.of(publicLink));

        assertThatThrownBy(() -> useCase.execute(token))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("WorkOrder");

        verify(budgetRepository, never()).findByWorkOrderId(any(UUID.class));
    }
}
