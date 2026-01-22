package br.com.baggiotech.tecos_api.presentation.mapper.equipment;

import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.presentation.dto.equipment.EquipmentResponse;
import org.springframework.stereotype.Component;

@Component
public class EquipmentMapper {
    
    public EquipmentResponse toResponse(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getCompany() != null ? equipment.getCompany().getId() : null,
                equipment.getCompany() != null ? equipment.getCompany().getName() : null,
                equipment.getClient() != null ? equipment.getClient().getId() : null,
                equipment.getClient() != null ? equipment.getClient().getName() : null,
                equipment.getType(),
                equipment.getBrand(),
                equipment.getModel(),
                equipment.getSerialNumber(),
                equipment.getObservations(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
    }
}
