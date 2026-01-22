package br.com.baggiotech.tecos_api.presentation.dto.budget;

import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID companyId,
        String companyName,
        UUID workOrderId,
        BudgetStatus status,
        BigDecimal serviceValue,
        BigDecimal partsValue,
        BigDecimal totalValue,
        String rejectionReason,
        UUID createdById,
        String createdByName,
        LocalDateTime approvedAt,
        String approvalMethod,
        UUID approvedById,
        String approvedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
