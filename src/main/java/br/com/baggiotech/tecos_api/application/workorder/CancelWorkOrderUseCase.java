package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CancelWorkOrderUseCase {

    private final WorkOrderRepository repository;
    private final WorkOrderHistoryRepository workOrderHistoryRepository;
    private final UserRepository userRepository;
    private final CustomMetrics metrics;

    public CancelWorkOrderUseCase(WorkOrderRepository repository,
                                 WorkOrderHistoryRepository workOrderHistoryRepository,
                                 UserRepository userRepository,
                                 CustomMetrics metrics) {
        this.repository = repository;
        this.workOrderHistoryRepository = workOrderHistoryRepository;
        this.userRepository = userRepository;
        this.metrics = metrics;
    }

    public WorkOrder execute(UUID id, UUID userId) {
        Timer.Sample sample = metrics.startTimer();
        try {
            WorkOrder workOrder = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("WorkOrder", id));

            // RB-03 e RB-04: Permite cancelar de qualquer status exceto ENTREGUE e CANCELADO
            if (!OrderStatus.isStatusTransitionAllowed(workOrder.getStatus(), OrderStatus.CANCELADO)) {
                throw new IllegalArgumentException(
                        "Não é possível cancelar uma ordem de serviço com status " + workOrder.getStatus() + "."
                );
            }

            OrderStatus statusBefore = workOrder.getStatus();
            workOrder.transitionTo(OrderStatus.CANCELADO);
            workOrder.setUpdatedAt(LocalDateTime.now());

            WorkOrder updated = repository.save(workOrder);

            // Criar histórico (RB-03)
            if (userId != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User", userId));
                
                WorkOrderHistory history = new WorkOrderHistory();
                history.setId(UUID.randomUUID());
                history.setWorkOrder(updated);
                history.setUser(user);
                history.setStatusBefore(statusBefore);
                history.setStatusAfter(OrderStatus.CANCELADO);
                history.setObservation("Ordem de serviço cancelada");
                history.setCreatedAt(LocalDateTime.now());
                history.setUpdatedAt(LocalDateTime.now());
                workOrderHistoryRepository.save(history);
            }
            metrics.incrementWorkOrdersUpdated();
            return updated;
        } finally {
            metrics.recordTimer(sample, "cancel");
        }
    }
}
