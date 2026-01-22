package br.com.baggiotech.tecos_api.presentation.dto.equipment;

import java.time.LocalDateTime;
import java.util.UUID;

public record EquipmentResponse(
        UUID id,
        UUID companyId,
        String companyName,
        UUID clientId,
        String clientName,
        String type,
        String brand,
        String model,
        String serialNumber,
        String observations,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
