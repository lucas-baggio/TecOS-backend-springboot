package br.com.baggiotech.tecos_api.presentation.dto.workorder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WorkOrderRequest(
        @NotNull(message = "Company ID é obrigatório")
        UUID companyId,
        
        @NotNull(message = "Client ID é obrigatório")
        UUID clientId,
        
        @NotNull(message = "Equipment ID é obrigatório")
        UUID equipmentId,
        
        @NotNull(message = "Technician ID é obrigatório")
        UUID technicianId,
        
        @NotBlank(message = "Defeito relatado é obrigatório")
        String reportedDefect,
        
        String internalObservations,
        
        Boolean returnOrder,
        
        UUID originWorkOrderId
) {
}
