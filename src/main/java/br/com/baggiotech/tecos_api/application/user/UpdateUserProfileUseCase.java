package br.com.baggiotech.tecos_api.application.user;

import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateUserProfileUseCase {

    private final UserRepository repository;

    public UpdateUserProfileUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public User execute(UUID userId, String name, String email) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        UUID companyId = user.getCompany() != null ? user.getCompany().getId() : null;

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }

        if (email != null && !email.isBlank()) {
            if (companyId != null && repository.existsByEmailAndCompanyIdAndIdNot(email, companyId, userId)) {
                throw new EntityAlreadyExistsException("User", "email", email);
            }
            user.setEmail(email);
        }

        user.setUpdatedAt(LocalDateTime.now());

        return repository.save(user);
    }
}
