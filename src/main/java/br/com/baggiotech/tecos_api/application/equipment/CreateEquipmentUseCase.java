package br.com.baggiotech.tecos_api.application.equipment;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateEquipmentUseCase {

    private final EquipmentRepository repository;
    private final ClientRepository clientRepository;
    private final CustomMetrics metrics;

    public CreateEquipmentUseCase(EquipmentRepository repository, ClientRepository clientRepository, CustomMetrics metrics) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.metrics = metrics;
    }

    public Equipment execute(UUID companyId, UUID clientId, String type, String brand, 
                            String model, String serialNumber, String observations) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("Client", clientId));

            // Verificar se o client pertence à mesma company
            if (client.getCompany() == null || !client.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("O cliente selecionado não pertence à empresa especificada.");
            }

            Company company = client.getCompany();

            Equipment equipment = new Equipment();
            equipment.setId(UUID.randomUUID());
            equipment.setCompany(company);
            equipment.setClient(client);
            equipment.setType(type);
            equipment.setBrand(brand);
            equipment.setModel(model);
            equipment.setSerialNumber(serialNumber);
            equipment.setObservations(observations);
            equipment.setCreatedAt(LocalDateTime.now());
            equipment.setUpdatedAt(LocalDateTime.now());
            equipment.setDeletedAt(null);

            Equipment saved = repository.save(equipment);
            metrics.incrementEquipmentsCreated();
            return saved;
        } finally {
            metrics.recordTimer(sample, "create");
        }
    }
}
