package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateCompanyUseCase {

    private final CompanyRepository repository;
    private final CustomMetrics metrics;

    public UpdateCompanyUseCase(CompanyRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public Company execute(UUID id, String name, String email, String whatsapp, String logoUrl, Boolean isActive) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Company company = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Company", id));

            if (email != null && !email.isBlank()) {
                if (!email.equals(company.getEmail()) && repository.existsByEmailAndIdNot(email, id)) {
                    throw new EntityAlreadyExistsException("Company", "email", email);
                }
                company.setEmail(email);
            }

            if (name != null && !name.isBlank()) {
                company.setName(name);
            }

            if (whatsapp != null) {
                company.setWhatsapp(whatsapp);
            }

            if (logoUrl != null) {
                company.setLogoUrl(logoUrl);
            }

            if (isActive != null) {
                company.setIsActive(isActive);
            }

            company.setUpdatedAt(LocalDateTime.now());

            Company saved = repository.save(company);
            metrics.incrementCompaniesUpdated();
            return saved;
        } finally {
            metrics.recordTimer(sample, "update");
        }
    }
}
