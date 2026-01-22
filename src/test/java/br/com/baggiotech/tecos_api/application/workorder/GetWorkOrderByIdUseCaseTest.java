package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWorkOrderByIdUseCase Tests")
class GetWorkOrderByIdUseCaseTest {

    @Mock
    private WorkOrderRepository repository;

    @InjectMocks
    private GetWorkOrderByIdUseCase useCase;

    private WorkOrder workOrder;
    private UUID workOrderId;

    @BeforeEach
    void setUp() {
        workOrderId = UUID.randomUUID();
        workOrder = new WorkOrder();
        workOrder.setId(workOrderId);
        workOrder.setStatus(OrderStatus.RECEBIDO);
    }

    @Test
    @DisplayName("Deve buscar work order por ID com sucesso")
    void shouldGetWorkOrderByIdSuccessfully() {
        when(repository.findById(workOrderId)).thenReturn(Optional.of(workOrder));

        WorkOrder result = useCase.execute(workOrderId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(workOrderId);
        verify(repository).findById(workOrderId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order não encontrado")
    void shouldThrowExceptionWhenWorkOrderNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("WorkOrder");

        verify(repository).findById(nonExistentId);
    }
}
