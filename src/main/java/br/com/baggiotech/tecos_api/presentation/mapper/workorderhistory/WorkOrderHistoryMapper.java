package br.com.baggiotech.tecos_api.presentation.mapper.workorderhistory;

import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.presentation.dto.workorderhistory.WorkOrderHistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderHistoryMapper {
    
    public WorkOrderHistoryResponse toResponse(WorkOrderHistory history) {
        return new WorkOrderHistoryResponse(
                history.getId(),
                history.getWorkOrder() != null ? history.getWorkOrder().getId() : null,
                history.getUser() != null ? history.getUser().getId() : null,
                history.getUser() != null ? history.getUser().getName() : null,
                history.getStatusBefore(),
                history.getStatusAfter(),
                history.getObservation(),
                history.getCreatedAt(),
                history.getUpdatedAt()
        );
    }
}
