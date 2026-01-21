package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCompanyUseCase {

    private final CompanyRepository repository;
    private final CustomMetrics metrics;

    public DeleteCompanyUseCase(CompanyRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public void execute(UUID id) {
        Timer.Sample sample = metrics.startTimer();
        try {
            if (!repository.existsById(id)) {
                throw new EntityNotFoundException("Company", id);
            }

            repository.deleteById(id);
            metrics.incrementCompaniesDeleted();
        } finally {
            metrics.recordTimer(sample, "delete");
        }
    }
}
