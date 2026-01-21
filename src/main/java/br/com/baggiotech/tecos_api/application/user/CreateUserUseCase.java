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
public class CreateUserUseCase {

    private final UserRepository repository;
    private final CompanyRepository companyRepository;
    private final CustomMetrics metrics;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCase(UserRepository repository, CompanyRepository companyRepository, 
                            CustomMetrics metrics, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.metrics = metrics;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(UUID companyId, String name, String email, String password, String type, Boolean isActive) {
        Timer.Sample sample = metrics.startTimer();
        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Company", companyId));

            if (email != null && !email.isBlank() && repository.existsByEmailAndCompanyId(email, companyId)) {
                throw new EntityAlreadyExistsException("User", "email", email);
            }

            if (type == null || (!type.equals("ADMIN") && !type.equals("TECNICO"))) {
                throw new IllegalArgumentException("Tipo deve ser ADMIN ou TECNICO");
            }

            User user = new User();
            user.setCompany(company);
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setType(type);
            user.setIsActive(isActive != null ? isActive : true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            return repository.save(user);
        } finally {
            metrics.recordTimer(sample, "create");
        }
    }
}
