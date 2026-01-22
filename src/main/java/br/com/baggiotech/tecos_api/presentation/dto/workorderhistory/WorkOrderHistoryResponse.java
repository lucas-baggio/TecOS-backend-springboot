package br.com.baggiotech.tecos_api.presentation.dto.workorderhistory;

import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record WorkOrderHistoryResponse(
        UUID id,
        UUID workOrderId,
        UUID userId,
        String userName,
        OrderStatus statusBefore,
        OrderStatus statusAfter,
        String observation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
