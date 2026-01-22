package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelWorkOrderUseCase Tests")
class CancelWorkOrderUseCaseTest {

    @Mock
    private WorkOrderRepository repository;

    @Mock
    private WorkOrderHistoryRepository workOrderHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private CancelWorkOrderUseCase useCase;

    private WorkOrder workOrder;
    private User user;
    private UUID workOrderId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        workOrderId = UUID.randomUUID();
        userId = UUID.randomUUID();

        workOrder = new WorkOrder();
        workOrder.setId(workOrderId);
        workOrder.setStatus(OrderStatus.RECEBIDO);

        user = new User();
        user.setId(userId);
    }

    private void setupMetrics() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = 
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        Timer.Sample sample = Timer.start(meterRegistry);
        lenient().when(metrics.startTimer()).thenReturn(sample);
        lenient().doNothing().when(metrics).incrementWorkOrdersUpdated();
        lenient().doNothing().when(metrics).recordTimer(any(Timer.Sample.class), anyString());
    }

    @Test
    @DisplayName("Deve cancelar work order com sucesso")
    void shouldCancelWorkOrderSuccessfully() {
        setupMetrics();
        when(repository.findById(workOrderId)).thenReturn(Optional.of(workOrder));
        when(repository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workOrderHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder result = useCase.execute(workOrderId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELADO);
        verify(repository).save(any(WorkOrder.class));
        verify(workOrderHistoryRepository).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order não encontrado")
    void shouldThrowExceptionWhenWorkOrderNotFound() {
        setupMetrics();
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("WorkOrder");

        verify(repository, never()).save(any(WorkOrder.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando não pode cancelar (status ENTREGUE)")
    void shouldThrowExceptionWhenCannotCancel() {
        setupMetrics();
        workOrder.setStatus(OrderStatus.ENTREGUE);
        when(repository.findById(workOrderId)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> useCase.execute(workOrderId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Não é possível cancelar");

        verify(repository, never()).save(any(WorkOrder.class));
    }

    @Test
    @DisplayName("Deve cancelar sem criar histórico quando userId é null")
    void shouldCancelWithoutHistoryWhenUserIdIsNull() {
        setupMetrics();
        when(repository.findById(workOrderId)).thenReturn(Optional.of(workOrder));
        when(repository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder result = useCase.execute(workOrderId, null);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELADO);
        verify(repository).save(any(WorkOrder.class));
        verify(workOrderHistoryRepository, never()).save(any());
    }
}
