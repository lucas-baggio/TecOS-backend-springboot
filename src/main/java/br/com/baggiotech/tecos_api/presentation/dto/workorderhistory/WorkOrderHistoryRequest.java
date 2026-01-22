package br.com.baggiotech.tecos_api.presentation.dto.workorderhistory;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WorkOrderHistoryRequest(
        @NotNull(message = "Work Order ID é obrigatório")
        UUID workOrderId,
        
        @NotNull(message = "User ID é obrigatório")
        UUID userId,
        
        String observation
) {
}
