package br.com.baggiotech.tecos_api.application.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetClientByIdUseCase Tests")
class GetClientByIdUseCaseTest {

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private GetClientByIdUseCase useCase;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Test Client");
        client.setPhone("11999999999");
        client.setIsActive(true);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve retornar cliente quando encontrado")
    void shouldReturnClientWhenFound() {
        when(repository.findById(client.getId())).thenReturn(Optional.of(client));
        
        Client result = useCase.execute(client.getId());
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(client.getId());
        assertThat(result.getName()).isEqualTo("Test Client");
        verify(repository).findById(client.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não encontrado")
    void shouldThrowExceptionWhenClientNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Client");
        
        verify(repository).findById(nonExistentId);
    }
}
