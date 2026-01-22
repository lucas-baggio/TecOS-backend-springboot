package br.com.baggiotech.tecos_api.presentation.dto.workorder;

import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record WorkOrderResponse(
        UUID id,
        UUID companyId,
        String companyName,
        UUID clientId,
        String clientName,
        UUID equipmentId,
        String equipmentType,
        UUID technicianId,
        String technicianName,
        OrderStatus status,
        String reportedDefect,
        String internalObservations,
        Boolean returnOrder,
        UUID originWorkOrderId,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
