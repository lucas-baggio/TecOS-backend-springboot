package br.com.baggiotech.tecos_api.application.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateClientUseCase {

    private final ClientRepository repository;
    private final CustomMetrics metrics;

    public UpdateClientUseCase(ClientRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public Client execute(UUID id, String name, String phone, String email, 
                         String cpf, String observations, Boolean isActive) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Client client = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Client", id));

            if (name != null) {
                client.setName(name);
            }
            if (phone != null) {
                client.setPhone(phone);
            }
            if (email != null) {
                client.setEmail(email);
            }
            if (cpf != null) {
                client.setCpf(cpf);
            }
            if (observations != null) {
                client.setObservations(observations);
            }
            if (isActive != null) {
                client.setIsActive(isActive);
            }
            client.setUpdatedAt(LocalDateTime.now());

            Client updated = repository.save(client);
            metrics.incrementClientsUpdated();
            return updated;
        } finally {
            metrics.recordTimer(sample, "update");
        }
    }
}
