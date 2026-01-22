package br.com.baggiotech.tecos_api.application.equipment;

import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetEquipmentByIdUseCase {

    private final EquipmentRepository repository;

    public GetEquipmentByIdUseCase(EquipmentRepository repository) {
        this.repository = repository;
    }

    public Equipment execute(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment", id));
    }
}
