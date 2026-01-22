package br.com.baggiotech.tecos_api.presentation.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EquipmentRequest(
        @NotNull(message = "Company ID é obrigatório")
        UUID companyId,
        
        @NotNull(message = "Client ID é obrigatório")
        UUID clientId,
        
        @NotBlank(message = "Tipo é obrigatório")
        @Size(max = 255, message = "Tipo deve ter no máximo 255 caracteres")
        String type,
        
        @Size(max = 255, message = "Marca deve ter no máximo 255 caracteres")
        String brand,
        
        @Size(max = 255, message = "Modelo deve ter no máximo 255 caracteres")
        String model,
        
        @Size(max = 255, message = "Número de série deve ter no máximo 255 caracteres")
        String serialNumber,
        
        String observations
) {
}
