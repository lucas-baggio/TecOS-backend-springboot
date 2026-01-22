package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListWorkOrdersUseCase Tests")
class ListWorkOrdersUseCaseTest {

    @Mock
    private WorkOrderRepository repository;

    @InjectMocks
    private ListWorkOrdersUseCase useCase;

    private WorkOrder workOrder1;
    private WorkOrder workOrder2;
    private UUID companyId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        workOrder1 = new WorkOrder();
        workOrder1.setId(UUID.randomUUID());
        workOrder1.setStatus(OrderStatus.RECEBIDO);

        workOrder2 = new WorkOrder();
        workOrder2.setId(UUID.randomUUID());
        workOrder2.setStatus(OrderStatus.EM_ANALISE);
    }

    @Test
    @DisplayName("Deve listar todas as work orders")
    void shouldListAllWorkOrders() {
        List<WorkOrder> workOrders = Arrays.asList(workOrder1, workOrder2);
        when(repository.findAll()).thenReturn(workOrders);

        Page<WorkOrder> result = useCase.execute(null, null, null, null, null, null, null, "createdAt", "desc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve filtrar por companyId")
    void shouldFilterByCompanyId() {
        List<WorkOrder> workOrders = Arrays.asList(workOrder1);
        when(repository.findByCompanyId(companyId)).thenReturn(workOrders);

        Page<WorkOrder> result = useCase.execute(companyId, null, null, null, null, null, null, "createdAt", "desc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByCompanyId(companyId);
    }

    @Test
    @DisplayName("Deve filtrar por clientId")
    void shouldFilterByClientId() {
        List<WorkOrder> workOrders = Arrays.asList(workOrder1);
        when(repository.findByClientId(clientId)).thenReturn(workOrders);

        Page<WorkOrder> result = useCase.execute(null, clientId, null, null, null, null, null, "createdAt", "desc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByClientId(clientId);
    }

    @Test
    @DisplayName("Deve filtrar por status")
    void shouldFilterByStatus() {
        List<WorkOrder> workOrders = Arrays.asList(workOrder1);
        when(repository.findByStatus(OrderStatus.RECEBIDO)).thenReturn(workOrders);

        Page<WorkOrder> result = useCase.execute(null, null, null, null, OrderStatus.RECEBIDO, null, null, "createdAt", "desc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByStatus(OrderStatus.RECEBIDO);
    }

    @Test
    @DisplayName("Deve filtrar por companyId e status")
    void shouldFilterByCompanyIdAndStatus() {
        List<WorkOrder> workOrders = Arrays.asList(workOrder1);
        when(repository.findByCompanyIdAndStatus(companyId, OrderStatus.RECEBIDO)).thenReturn(workOrders);

        Page<WorkOrder> result = useCase.execute(companyId, null, null, null, OrderStatus.RECEBIDO, null, null, "createdAt", "desc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByCompanyIdAndStatus(companyId, OrderStatus.RECEBIDO);
    }

    @Test
    @DisplayName("Deve aplicar paginação")
    void shouldApplyPagination() {
        List<WorkOrder> workOrders = Arrays.asList(workOrder1, workOrder2);
        when(repository.findAll()).thenReturn(workOrders);

        Page<WorkOrder> result = useCase.execute(null, null, null, null, null, null, null, "createdAt", "desc", 0, 1);

        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}
