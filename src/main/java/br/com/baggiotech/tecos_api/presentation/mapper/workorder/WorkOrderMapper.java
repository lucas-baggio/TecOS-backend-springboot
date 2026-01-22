package br.com.baggiotech.tecos_api.presentation.mapper.workorder;

import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.presentation.dto.workorder.WorkOrderResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderMapper {
    
    public WorkOrderResponse toResponse(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getCompany() != null ? workOrder.getCompany().getId() : null,
                workOrder.getCompany() != null ? workOrder.getCompany().getName() : null,
                workOrder.getClient() != null ? workOrder.getClient().getId() : null,
                workOrder.getClient() != null ? workOrder.getClient().getName() : null,
                workOrder.getEquipment() != null ? workOrder.getEquipment().getId() : null,
                workOrder.getEquipment() != null ? workOrder.getEquipment().getType() : null,
                workOrder.getTechnician() != null ? workOrder.getTechnician().getId() : null,
                workOrder.getTechnician() != null ? workOrder.getTechnician().getName() : null,
                workOrder.getStatus(),
                workOrder.getReportedDefect(),
                workOrder.getInternalObservations(),
                workOrder.getReturnOrder(),
                workOrder.getOriginWorkOrderId(),
                workOrder.getDeliveredAt(),
                workOrder.getCreatedAt(),
                workOrder.getUpdatedAt()
        );
    }
}
