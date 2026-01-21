package br.com.baggiotech.tecos_api.application.user;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteUserUseCase {

    private final UserRepository repository;
    private final CustomMetrics metrics;

    public DeleteUserUseCase(UserRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public void execute(UUID id) {
        Timer.Sample sample = metrics.startTimer();
        try {
            if (!repository.existsById(id)) {
                throw new EntityNotFoundException("User", id);
            }

            repository.deleteById(id);
        } finally {
            metrics.recordTimer(sample, "delete");
        }
    }
}
