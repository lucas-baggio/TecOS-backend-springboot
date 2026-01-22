package br.com.baggiotech.tecos_api.application.equipment;

import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteEquipmentUseCase {

    private final EquipmentRepository repository;
    private final CustomMetrics metrics;

    public DeleteEquipmentUseCase(EquipmentRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public void execute(UUID id) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Equipment equipment = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Equipment", id));

            // Verificar se o equipment tem work orders associados antes de excluir
            long workOrderCount = repository.countByEquipmentId(id);
            if (workOrderCount > 0) {
                throw new IllegalArgumentException("Não é possível excluir um equipamento que possui ordens de serviço associadas.");
            }

            repository.delete(equipment);
            metrics.incrementEquipmentsDeleted();
        } finally {
            metrics.recordTimer(sample, "delete");
        }
    }
}
