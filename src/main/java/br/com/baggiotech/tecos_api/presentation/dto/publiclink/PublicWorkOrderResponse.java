package br.com.baggiotech.tecos_api.presentation.dto.publiclink;

import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PublicWorkOrderResponse(
        PublicWorkOrderInfo workOrder,
        PublicClientInfo client,
        PublicEquipmentInfo equipment,
        List<PublicBudgetInfo> budgets
) {
    public record PublicWorkOrderInfo(
            UUID id,
            OrderStatus status,
            String reportedDefect,
            Boolean returnOrder,
            LocalDateTime deliveredAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record PublicClientInfo(
            String name,
            String phone,
            String email
    ) {}

    public record PublicEquipmentInfo(
            String type,
            String brand,
            String model,
            String serialNumber
    ) {}

    public record PublicBudgetInfo(
            UUID id,
            BigDecimal serviceValue,
            BigDecimal partsValue,
            BigDecimal totalValue,
            br.com.baggiotech.tecos_api.domain.budget.BudgetStatus status,
            String rejectionReason,
            LocalDateTime createdAt,
            LocalDateTime approvedAt
    ) {}
}
