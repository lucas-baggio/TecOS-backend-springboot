package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateCompanyUseCase {

    private final CompanyRepository repository;
    private final CustomMetrics metrics;

    public CreateCompanyUseCase(CompanyRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public Company execute(String name, String email, String whatsapp, String logoUrl, Boolean isActive) {
        Timer.Sample sample = metrics.startTimer();
        try {
            if (email != null && !email.isBlank() && repository.existsByEmail(email)) {
                throw new EntityAlreadyExistsException("Company", "email", email);
            }

            Company company = new Company();
            company.setId(UUID.randomUUID());
            company.setName(name);
            company.setEmail(email);
            company.setWhatsapp(whatsapp);
            company.setLogoUrl(logoUrl);
            company.setIsActive(isActive != null ? isActive : true);
            company.setCreatedAt(LocalDateTime.now());
            company.setUpdatedAt(LocalDateTime.now());

            Company saved = repository.save(company);
            metrics.incrementCompaniesCreated();
            return saved;
        } finally {
            metrics.recordTimer(sample, "create");
        }
    }
}
