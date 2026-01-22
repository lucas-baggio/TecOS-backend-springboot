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
@DisplayName("GetPublicLinkByIdUseCase Tests")
class GetPublicLinkByIdUseCaseTest {

    @Mock
    private PublicLinkRepository repository;

    @InjectMocks
    private GetPublicLinkByIdUseCase useCase;

    private PublicLink publicLink;
    private UUID linkId;

    @BeforeEach
    void setUp() {
        linkId = UUID.randomUUID();
        publicLink = new PublicLink();
        publicLink.setId(linkId);
        publicLink.setToken("test-token");
    }

    @Test
    @DisplayName("Deve buscar link público por ID com sucesso")
    void shouldGetPublicLinkByIdSuccessfully() {
        when(repository.findById(linkId)).thenReturn(Optional.of(publicLink));

        PublicLink result = useCase.execute(linkId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(linkId);
        verify(repository).findById(linkId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando link não encontrado")
    void shouldThrowExceptionWhenLinkNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("PublicLink");

        verify(repository).findById(nonExistentId);
    }
}
