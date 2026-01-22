package br.com.baggiotech.tecos_api.application.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateClientUseCase Tests")
class UpdateClientUseCaseTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private UpdateClientUseCase useCase;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Original Name");
        client.setPhone("11999999999");
        client.setEmail("original@example.com");
        client.setCpf("123.456.789-00");
        client.setIsActive(true);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void shouldUpdateClientSuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(client.getId())).thenReturn(Optional.of(client));
        when(repository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.startTimer()).thenReturn(sample);
        
        Client result = useCase.execute(
                client.getId(),
                "Updated Name",
                "11888888888",
                "updated@example.com",
                "987.654.321-00",
                "Updated observations",
                false
        );
        
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPhone()).isEqualTo("11888888888");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getCpf()).isEqualTo("987.654.321-00");
        assertThat(result.getIsActive()).isFalse();

        verify(repository).findById(client.getId());
        verify(repository).save(any(Client.class));
        verify(metrics).incrementClientsUpdated();
    }

    @Test
    @DisplayName("Deve atualizar apenas campos fornecidos")
    void shouldUpdateOnlyProvidedFields() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(client.getId())).thenReturn(Optional.of(client));
        when(repository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.startTimer()).thenReturn(sample);
        
        Client result = useCase.execute(
                client.getId(),
                "Updated Name",
                null,
                null,
                null,
                null,
                null
        );
        
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPhone()).isEqualTo("11999999999");
        assertThat(result.getEmail()).isEqualTo("original@example.com");

        verify(repository).save(any(Client.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não encontrado")
    void shouldThrowExceptionWhenClientNotFound() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());
        when(metrics.startTimer()).thenReturn(sample);
        
        assertThatThrownBy(() -> useCase.execute(
                nonExistentId,
                "Updated Name",
                null,
                null,
                null,
                null,
                null
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Client");

        verify(repository).findById(nonExistentId);
        verify(repository, never()).save(any(Client.class));
    }
}
