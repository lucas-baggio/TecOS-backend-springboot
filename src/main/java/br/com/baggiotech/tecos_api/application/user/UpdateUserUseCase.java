package br.com.baggiotech.tecos_api.application.user;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import br.com.baggiotech.tecos_api.infrastructure.security.PasswordEncoder;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateUserUseCase {

    private final UserRepository repository;
    private final CompanyRepository companyRepository;
    private final CustomMetrics metrics;
    private final PasswordEncoder passwordEncoder;

    public UpdateUserUseCase(UserRepository repository, CompanyRepository companyRepository, 
                            CustomMetrics metrics, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.metrics = metrics;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(UUID id, UUID companyId, String name, String email, String password, String type, Boolean isActive) {
        Timer.Sample sample = metrics.startTimer();
        try {
            User user = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User", id));

            UUID currentCompanyId = user.getCompany() != null ? user.getCompany().getId() : null;
            UUID targetCompanyId = companyId != null ? companyId : currentCompanyId;

            if (companyId != null && !companyId.equals(currentCompanyId)) {
                Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new EntityNotFoundException("Company", companyId));
                user.setCompany(company);
            }

            if (name != null && !name.isBlank()) {
                user.setName(name);
            }

            if (email != null && !email.isBlank()) {
                if (targetCompanyId != null && repository.existsByEmailAndCompanyIdAndIdNot(email, targetCompanyId, id)) {
                    throw new EntityAlreadyExistsException("User", "email", email);
                }
                user.setEmail(email);
            }

            if (password != null && !password.isBlank()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            if (type != null && !type.isBlank()) {
                if (!type.equals("ADMIN") && !type.equals("TECNICO")) {
                    throw new IllegalArgumentException("Tipo deve ser ADMIN ou TECNICO");
                }
                user.setType(type);
            }

            if (isActive != null) {
                user.setIsActive(isActive);
            }

            user.setUpdatedAt(LocalDateTime.now());

            return repository.save(user);
        } finally {
            metrics.recordTimer(sample, "update");
        }
    }
}
