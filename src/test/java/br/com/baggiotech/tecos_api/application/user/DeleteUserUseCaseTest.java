package br.com.baggiotech.tecos_api.application.user;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUserUseCase Tests")
class DeleteUserUseCaseTest {

    @Mock
    private UserRepository repository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private DeleteUserUseCase useCase;

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        UUID userId = UUID.randomUUID();
        when(repository.existsById(userId)).thenReturn(true);
        doNothing().when(repository).deleteById(userId);
        when(metrics.startTimer()).thenReturn(sample);

        useCase.execute(userId);

        verify(repository).existsById(userId);
        verify(repository).deleteById(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        UUID nonExistentId = UUID.randomUUID();
        when(repository.existsById(nonExistentId)).thenReturn(false);
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining(nonExistentId.toString());

        verify(repository).existsById(nonExistentId);
        verify(repository, never()).deleteById(any(UUID.class));
    }
}
