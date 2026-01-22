package br.com.baggiotech.tecos_api.application.workorderhistory;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
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
@DisplayName("CreateWorkOrderHistoryUseCase Tests")
class CreateWorkOrderHistoryUseCaseTest {

    @Mock
    private WorkOrderHistoryRepository repository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateWorkOrderHistoryUseCase useCase;

    private Company company;
    private User user;
    private WorkOrder workOrder;
    private WorkOrderHistory savedHistory;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Test Company");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setCompany(company);
        user.setName("Test User");

        workOrder = new WorkOrder();
        workOrder.setId(UUID.randomUUID());
        workOrder.setCompany(company);
        workOrder.setStatus(OrderStatus.RECEBIDO);

        savedHistory = new WorkOrderHistory();
        savedHistory.setId(UUID.randomUUID());
        savedHistory.setWorkOrder(workOrder);
        savedHistory.setUser(user);
        savedHistory.setStatusBefore(null);
        savedHistory.setStatusAfter(OrderStatus.RECEBIDO);
        savedHistory.setObservation("Test observation");
    }

    @Test
    @DisplayName("Deve criar histórico com sucesso")
    void shouldCreateHistorySuccessfully() {
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(repository.save(any(WorkOrderHistory.class))).thenReturn(savedHistory);

        WorkOrderHistory result = useCase.execute(
                workOrder.getId(),
                user.getId(),
                null,
                OrderStatus.RECEBIDO,
                "Test observation"
        );

        assertThat(result).isNotNull();
        assertThat(result.getStatusBefore()).isNull();
        assertThat(result.getStatusAfter()).isEqualTo(OrderStatus.RECEBIDO);
        assertThat(result.getObservation()).isEqualTo("Test observation");

        verify(workOrderRepository).findById(workOrder.getId());
        verify(userRepository).findById(user.getId());
        verify(repository).save(any(WorkOrderHistory.class));
    }

    @Test
    @DisplayName("Deve criar histórico com statusBefore")
    void shouldCreateHistoryWithStatusBefore() {
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(repository.save(any(WorkOrderHistory.class))).thenAnswer(invocation -> {
            WorkOrderHistory history = invocation.getArgument(0);
            assertThat(history.getStatusBefore()).isEqualTo(OrderStatus.RECEBIDO);
            assertThat(history.getStatusAfter()).isEqualTo(OrderStatus.EM_ANALISE);
            return savedHistory;
        });

        useCase.execute(
                workOrder.getId(),
                user.getId(),
                OrderStatus.RECEBIDO,
                OrderStatus.EM_ANALISE,
                "Status changed"
        );

        verify(repository).save(any(WorkOrderHistory.class));
    }

    @Test
    @DisplayName("Deve criar histórico sem observation")
    void shouldCreateHistoryWithoutObservation() {
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(repository.save(any(WorkOrderHistory.class))).thenAnswer(invocation -> {
            WorkOrderHistory history = invocation.getArgument(0);
            assertThat(history.getObservation()).isNull();
            return savedHistory;
        });

        useCase.execute(
                workOrder.getId(),
                user.getId(),
                null,
                OrderStatus.RECEBIDO,
                null
        );

        verify(repository).save(any(WorkOrderHistory.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order não existe")
    void shouldThrowExceptionWhenWorkOrderNotFound() {
        UUID nonExistentWorkOrderId = UUID.randomUUID();
        when(workOrderRepository.findById(nonExistentWorkOrderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                nonExistentWorkOrderId,
                user.getId(),
                null,
                OrderStatus.RECEBIDO,
                "Test"
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("WorkOrder");

        verify(workOrderRepository).findById(nonExistentWorkOrderId);
        verify(repository, never()).save(any(WorkOrderHistory.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando user não existe")
    void shouldThrowExceptionWhenUserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                workOrder.getId(),
                nonExistentUserId,
                null,
                OrderStatus.RECEBIDO,
                "Test"
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User");

        verify(userRepository).findById(nonExistentUserId);
        verify(repository, never()).save(any(WorkOrderHistory.class));
    }
}
