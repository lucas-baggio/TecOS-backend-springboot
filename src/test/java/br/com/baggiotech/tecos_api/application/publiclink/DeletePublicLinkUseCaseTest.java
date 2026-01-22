package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePublicLinkUseCase Tests")
class DeletePublicLinkUseCaseTest {

    @Mock
    private PublicLinkRepository repository;

    @InjectMocks
    private DeletePublicLinkUseCase useCase;

    private Company company;
    private WorkOrder workOrder;
    private PublicLink publicLink;
    private UUID linkId;

    @BeforeEach
    void setUp() {
        linkId = UUID.randomUUID();
        company = new Company();
        company.setId(UUID.randomUUID());

        workOrder = new WorkOrder();
        workOrder.setId(UUID.randomUUID());
        workOrder.setCompany(company);

        publicLink = new PublicLink();
        publicLink.setId(linkId);
        publicLink.setWorkOrder(workOrder);
        publicLink.setToken("test-token");
    }

    @Test
    @DisplayName("Deve deletar link público com sucesso")
    void shouldDeletePublicLinkSuccessfully() {
        when(repository.findById(linkId)).thenReturn(Optional.of(publicLink));
        doNothing().when(repository).delete(publicLink);

        useCase.execute(linkId, company.getId());

        verify(repository).findById(linkId);
        verify(repository).delete(publicLink);
    }

    @Test
    @DisplayName("Deve lançar exceção quando link não encontrado")
    void shouldThrowExceptionWhenLinkNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId, company.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("PublicLink");

        verify(repository, never()).delete(any(PublicLink.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando link não pertence à company")
    void shouldThrowExceptionWhenLinkDoesNotBelongToCompany() {
        Company otherCompany = new Company();
        otherCompany.setId(UUID.randomUUID());
        workOrder.setCompany(otherCompany);

        when(repository.findById(linkId)).thenReturn(Optional.of(publicLink));

        assertThatThrownBy(() -> useCase.execute(linkId, company.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");

        verify(repository, never()).delete(any(PublicLink.class));
    }
}
