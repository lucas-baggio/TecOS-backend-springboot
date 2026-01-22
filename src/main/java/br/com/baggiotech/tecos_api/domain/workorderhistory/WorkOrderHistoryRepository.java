package br.com.baggiotech.tecos_api.domain.workorderhistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderHistoryRepository {
    WorkOrderHistory save(WorkOrderHistory workOrderHistory);
    Optional<WorkOrderHistory> findById(UUID id);
    boolean existsById(UUID id);
    List<WorkOrderHistory> findAll();
    List<WorkOrderHistory> findByWorkOrderId(UUID workOrderId);
    List<WorkOrderHistory> findByUserId(UUID userId);
    List<WorkOrderHistory> findByWorkOrderIdOrderByCreatedAtDesc(UUID workOrderId);
}
