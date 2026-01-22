package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPublicLinkByTokenUseCase Tests")
class GetPublicLinkByTokenUseCaseTest {

    @Mock
    private PublicLinkRepository repository;

    @InjectMocks
    private GetPublicLinkByTokenUseCase useCase;

    private PublicLink publicLink;
    private String token;

    @BeforeEach
    void setUp() {
        token = "test-token-123";
        publicLink = new PublicLink();
        publicLink.setId(UUID.randomUUID());
        publicLink.setToken(token);
    }

    @Test
    @DisplayName("Deve buscar link público por token com sucesso")
    void shouldGetPublicLinkByTokenSuccessfully() {
        when(repository.findByToken(token)).thenReturn(Optional.of(publicLink));

        PublicLink result = useCase.execute(token);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(token);
        verify(repository).findByToken(token);
    }

    @Test
    @DisplayName("Deve lançar exceção quando token não encontrado")
    void shouldThrowExceptionWhenTokenNotFound() {
        String nonExistentToken = "non-existent-token";
        when(repository.findByToken(nonExistentToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentToken))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("PublicLink");

        verify(repository).findByToken(nonExistentToken);
    }
}
