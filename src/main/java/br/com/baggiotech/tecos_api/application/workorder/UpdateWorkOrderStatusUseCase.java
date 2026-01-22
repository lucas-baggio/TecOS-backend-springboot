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
public class UpdateWorkOrderStatusUseCase {

    private final WorkOrderRepository repository;
    private final WorkOrderHistoryRepository workOrderHistoryRepository;
    private final UserRepository userRepository;
    private final CustomMetrics metrics;

    public UpdateWorkOrderStatusUseCase(WorkOrderRepository repository,
                                       WorkOrderHistoryRepository workOrderHistoryRepository,
                                       UserRepository userRepository,
                                       CustomMetrics metrics) {
        this.repository = repository;
        this.workOrderHistoryRepository = workOrderHistoryRepository;
        this.userRepository = userRepository;
        this.metrics = metrics;
    }

    public WorkOrder execute(UUID id, OrderStatus newStatus, String observation, UUID userId) {
        Timer.Sample sample = metrics.startTimer();
        try {
            WorkOrder workOrder = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("WorkOrder", id));

            // RB-03: Não pode editar OS CANCELADA ou ENTREGUE
            if (workOrder.getStatus() == OrderStatus.CANCELADO || workOrder.getStatus() == OrderStatus.ENTREGUE) {
                throw new IllegalArgumentException(
                        "Não é possível alterar status de uma ordem de serviço com status " + workOrder.getStatus() + "."
                );
            }

            // RB-05: Validar se há orçamento aprovado antes de permitir EM_CONSERTO
            // TODO: Implementar quando Budget for criado
            // if (newStatus == OrderStatus.EM_CONSERTO) {
            //     Budget approvedBudget = budgetRepository.findApprovedByWorkOrderId(id);
            //     if (approvedBudget == null) {
            //         throw new IllegalArgumentException("Não é possível mudar para EM_CONSERTO sem um orçamento aprovado.");
            //     }
            // }

            OrderStatus statusBefore = workOrder.getStatus();
            workOrder.transitionTo(newStatus);
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
                history.setStatusAfter(newStatus);
                history.setObservation(observation);
                history.setCreatedAt(LocalDateTime.now());
                history.setUpdatedAt(LocalDateTime.now());
                workOrderHistoryRepository.save(history);
            }
            metrics.incrementWorkOrdersUpdated();
            return updated;
        } finally {
            metrics.recordTimer(sample, "updateStatus");
        }
    }
}
