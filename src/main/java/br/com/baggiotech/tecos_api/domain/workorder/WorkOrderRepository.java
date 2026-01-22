package br.com.baggiotech.tecos_api.domain.workorder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderRepository {
    WorkOrder save(WorkOrder workOrder);
    Optional<WorkOrder> findById(UUID id);
    boolean existsById(UUID id);
    void delete(WorkOrder workOrder);
    List<WorkOrder> findAll();
    List<WorkOrder> findByCompanyId(UUID companyId);
    List<WorkOrder> findByClientId(UUID clientId);
    List<WorkOrder> findByEquipmentId(UUID equipmentId);
    List<WorkOrder> findByTechnicianId(UUID technicianId);
    List<WorkOrder> findByStatus(OrderStatus status);
    List<WorkOrder> findByCompanyIdAndStatus(UUID companyId, OrderStatus status);
    List<WorkOrder> findByClientIdAndStatus(UUID clientId, OrderStatus status);
    List<WorkOrder> findByReturnOrder(Boolean returnOrder);
    List<WorkOrder> searchByReportedDefectOrInternalObservationsOrClientName(String search);
}
