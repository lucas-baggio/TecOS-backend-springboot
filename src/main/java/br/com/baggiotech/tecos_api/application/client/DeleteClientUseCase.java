package br.com.baggiotech.tecos_api.application.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteClientUseCase {

    private final ClientRepository repository;
    private final CustomMetrics metrics;

    public DeleteClientUseCase(ClientRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public void execute(UUID id) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Client client = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Client", id));

            // TODO: Verificar se o client tem equipamentos associados antes de excluir
            // Isso será implementado quando a entidade Equipment for criada
            // if (client.hasEquipments()) {
            //     throw new ValidationException("Não é possível excluir um cliente que possui equipamentos associados.");
            // }

            repository.delete(client);
            metrics.incrementClientsDeleted();
        } finally {
            metrics.recordTimer(sample, "delete");
        }
    }
}
