package br.com.baggiotech.tecos_api.application.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateClientUseCase {

    private final ClientRepository repository;
    private final CompanyRepository companyRepository;
    private final CustomMetrics metrics;

    public CreateClientUseCase(ClientRepository repository, CompanyRepository companyRepository, CustomMetrics metrics) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.metrics = metrics;
    }

    public Client execute(UUID companyId, String name, String phone, String email, 
                         String cpf, String observations, Boolean isActive) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Company", companyId));

            Client client = new Client();
            client.setId(UUID.randomUUID());
            client.setCompany(company);
            client.setName(name);
            client.setPhone(phone);
            client.setEmail(email);
            client.setCpf(cpf);
            client.setObservations(observations);
            client.setIsActive(isActive != null ? isActive : true);
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
            client.setDeletedAt(null);

            Client saved = repository.save(client);
            metrics.incrementClientsCreated();
            return saved;
        } finally {
            metrics.recordTimer(sample, "create");
        }
    }
}
