package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPublicLinksUseCase Tests")
class ListPublicLinksUseCaseTest {

    @Mock
    private PublicLinkRepository repository;

    @InjectMocks
    private ListPublicLinksUseCase useCase;

    private PublicLink link1;
    private PublicLink link2;
    private UUID workOrderId;

    @BeforeEach
    void setUp() {
        workOrderId = UUID.randomUUID();

        link1 = new PublicLink();
        link1.setId(UUID.randomUUID());
        link1.setToken("token-1");

        link2 = new PublicLink();
        link2.setId(UUID.randomUUID());
        link2.setToken("token-2");
    }

    @Test
    @DisplayName("Deve listar todos os links públicos")
    void shouldListAllPublicLinks() {
        List<PublicLink> links = Arrays.asList(link1, link2);
        when(repository.findAll()).thenReturn(links);

        List<PublicLink> result = useCase.execute(null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve filtrar por workOrderId")
    void shouldFilterByWorkOrderId() {
        List<PublicLink> links = Arrays.asList(link1);
        when(repository.findByWorkOrderId(workOrderId)).thenReturn(links);

        List<PublicLink> result = useCase.execute(workOrderId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(repository).findByWorkOrderId(workOrderId);
        verify(repository, never()).findAll();
    }
}
