package br.com.baggiotech.tecos_api.application.workorderhistory;

import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateWorkOrderHistoryUseCase {

    private final WorkOrderHistoryRepository repository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    public CreateWorkOrderHistoryUseCase(WorkOrderHistoryRepository repository,
                                        WorkOrderRepository workOrderRepository,
                                        UserRepository userRepository) {
        this.repository = repository;
        this.workOrderRepository = workOrderRepository;
        this.userRepository = userRepository;
    }

    public WorkOrderHistory execute(UUID workOrderId, UUID userId, OrderStatus statusBefore,
                                   OrderStatus statusAfter, String observation) {
        // Buscar work order
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder", workOrderId));

        // Buscar user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        // Criar histórico
        WorkOrderHistory history = new WorkOrderHistory();
        history.setId(UUID.randomUUID());
        history.setWorkOrder(workOrder);
        history.setUser(user);
        history.setStatusBefore(statusBefore);
        history.setStatusAfter(statusAfter);
        history.setObservation(observation);
        history.setCreatedAt(LocalDateTime.now());
        history.setUpdatedAt(LocalDateTime.now());

        return repository.save(history);
    }
}
