package br.com.baggiotech.tecos_api.application.workorderhistory;

import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListWorkOrderHistoriesUseCase Tests")
class ListWorkOrderHistoriesUseCaseTest {

    @Mock
    private WorkOrderHistoryRepository repository;

    @InjectMocks
    private ListWorkOrderHistoriesUseCase useCase;

    private WorkOrderHistory history1;
    private WorkOrderHistory history2;
    private UUID workOrderId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        workOrderId = UUID.randomUUID();
        userId = UUID.randomUUID();

        history1 = new WorkOrderHistory();
        history1.setId(UUID.randomUUID());
        history1.setStatusAfter(OrderStatus.RECEBIDO);
        history1.setCreatedAt(LocalDateTime.now().minusHours(2));

        history2 = new WorkOrderHistory();
        history2.setId(UUID.randomUUID());
        history2.setStatusAfter(OrderStatus.EM_ANALISE);
        history2.setCreatedAt(LocalDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("Deve listar todos os históricos")
    void shouldListAllHistories() {
        List<WorkOrderHistory> histories = Arrays.asList(history1, history2);
        when(repository.findAll()).thenReturn(histories);

        Page<WorkOrderHistory> result = useCase.execute(null, null, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve filtrar por workOrderId")
    void shouldFilterByWorkOrderId() {
        List<WorkOrderHistory> histories = Arrays.asList(history1);
        when(repository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId)).thenReturn(histories);

        Page<WorkOrderHistory> result = useCase.execute(workOrderId, null, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByWorkOrderIdOrderByCreatedAtDesc(workOrderId);
    }

    @Test
    @DisplayName("Deve filtrar por userId")
    void shouldFilterByUserId() {
        List<WorkOrderHistory> histories = Arrays.asList(history1, history2);
        when(repository.findByUserId(userId)).thenReturn(histories);

        Page<WorkOrderHistory> result = useCase.execute(null, userId, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findByUserId(userId);
    }

    @Test
    @DisplayName("Deve filtrar por workOrderId e userId")
    void shouldFilterByWorkOrderIdAndUserId() {
        List<WorkOrderHistory> byWorkOrder = Arrays.asList(history1, history2);
        List<WorkOrderHistory> byUser = Arrays.asList(history1);
        when(repository.findByWorkOrderId(workOrderId)).thenReturn(byWorkOrder);
        when(repository.findByUserId(userId)).thenReturn(byUser);

        Page<WorkOrderHistory> result = useCase.execute(workOrderId, userId, "created_at", "desc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByWorkOrderId(workOrderId);
        verify(repository).findByUserId(userId);
    }

    @Test
    @DisplayName("Deve paginar resultados corretamente")
    void shouldPaginateResultsCorrectly() {
        List<WorkOrderHistory> histories = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            WorkOrderHistory history = new WorkOrderHistory();
            history.setId(UUID.randomUUID());
            history.setCreatedAt(LocalDateTime.now().minusHours(i));
            histories.add(history);
        }
        when(repository.findAll()).thenReturn(histories);

        Page<WorkOrderHistory> result = useCase.execute(null, null, "created_at", "desc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
    }

    @Test
    @DisplayName("Deve ordenar por status_after")
    void shouldSortByStatusAfter() {
        List<WorkOrderHistory> histories = Arrays.asList(history2, history1);
        when(repository.findAll()).thenReturn(histories);

        Page<WorkOrderHistory> result = useCase.execute(null, null, "status_after", "asc", 0, 15);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll();
    }
}
