package br.com.baggiotech.tecos_api.application.equipment;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateEquipmentUseCase {

    private final EquipmentRepository repository;
    private final ClientRepository clientRepository;
    private final CustomMetrics metrics;

    public UpdateEquipmentUseCase(EquipmentRepository repository, ClientRepository clientRepository, CustomMetrics metrics) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.metrics = metrics;
    }

    public Equipment execute(UUID id, UUID companyId, UUID clientId, String type, 
                            String brand, String model, String serialNumber, String observations) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Equipment equipment = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Equipment", id));

            // Se client_id foi alterado, verificar se pertence à mesma company
            if (clientId != null && !clientId.equals(equipment.getClient().getId())) {
                Client client = clientRepository.findById(clientId)
                        .orElseThrow(() -> new EntityNotFoundException("Client", clientId));
                
                if (client.getCompany() == null || !client.getCompany().getId().equals(companyId)) {
                    throw new IllegalArgumentException("O cliente selecionado não pertence à empresa especificada.");
                }
                equipment.setClient(client);
            }

            if (type != null) {
                equipment.setType(type);
            }
            if (brand != null) {
                equipment.setBrand(brand);
            }
            if (model != null) {
                equipment.setModel(model);
            }
            if (serialNumber != null) {
                equipment.setSerialNumber(serialNumber);
            }
            if (observations != null) {
                equipment.setObservations(observations);
            }
            equipment.setUpdatedAt(LocalDateTime.now());

            Equipment updated = repository.save(equipment);
            metrics.incrementEquipmentsUpdated();
            return updated;
        } finally {
            metrics.recordTimer(sample, "update");
        }
    }
}
