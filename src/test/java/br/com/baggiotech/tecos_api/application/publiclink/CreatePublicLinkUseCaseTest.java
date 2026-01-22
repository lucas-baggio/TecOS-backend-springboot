package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePublicLinkUseCase Tests")
class CreatePublicLinkUseCaseTest {

    @Mock
    private PublicLinkRepository repository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @InjectMocks
    private CreatePublicLinkUseCase useCase;

    private Company company;
    private WorkOrder workOrder;
    private PublicLink savedLink;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Test Company");

        workOrder = new WorkOrder();
        workOrder.setId(UUID.randomUUID());
        workOrder.setCompany(company);
        workOrder.setStatus(OrderStatus.RECEBIDO);

        savedLink = new PublicLink();
        savedLink.setId(UUID.randomUUID());
        savedLink.setWorkOrder(workOrder);
        savedLink.setToken("test-token-123");
    }

    @Test
    @DisplayName("Deve criar link público com sucesso")
    void shouldCreatePublicLinkSuccessfully() {
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(repository.existsByToken(any(String.class))).thenReturn(false);
        when(repository.save(any(PublicLink.class))).thenReturn(savedLink);

        PublicLink result = useCase.execute(workOrder.getId(), company.getId());

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getToken().length()).isGreaterThan(0);

        verify(workOrderRepository).findById(workOrder.getId());
        verify(repository).save(any(PublicLink.class));
    }

    @Test
    @DisplayName("Deve gerar token único")
    void shouldGenerateUniqueToken() {
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(repository.existsByToken(any(String.class))).thenReturn(false);
        when(repository.save(any(PublicLink.class))).thenAnswer(invocation -> {
            PublicLink link = invocation.getArgument(0);
            assertThat(link.getToken()).isNotNull();
            assertThat(link.getToken().length()).isGreaterThan(0);
            return savedLink;
        });

        useCase.execute(workOrder.getId(), company.getId());

        verify(repository).save(any(PublicLink.class));
    }

    @Test
    @DisplayName("Deve gerar novo token se já existir")
    void shouldGenerateNewTokenIfExists() {
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(repository.existsByToken(any(String.class)))
                .thenReturn(true)  // Primeira tentativa: token existe
                .thenReturn(false); // Segunda tentativa: token único
        when(repository.save(any(PublicLink.class))).thenReturn(savedLink);

        PublicLink result = useCase.execute(workOrder.getId(), company.getId());

        assertThat(result).isNotNull();
        verify(repository, atLeast(2)).existsByToken(any(String.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order não existe")
    void shouldThrowExceptionWhenWorkOrderNotFound() {
        UUID nonExistentWorkOrderId = UUID.randomUUID();
        when(workOrderRepository.findById(nonExistentWorkOrderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentWorkOrderId, company.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("WorkOrder");

        verify(workOrderRepository).findById(nonExistentWorkOrderId);
        verify(repository, never()).save(any(PublicLink.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando work order não pertence à company")
    void shouldThrowExceptionWhenWorkOrderDoesNotBelongToCompany() {
        Company otherCompany = new Company();
        otherCompany.setId(UUID.randomUUID());
        workOrder.setCompany(otherCompany);

        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> useCase.execute(workOrder.getId(), company.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pertence à sua empresa");

        verify(repository, never()).save(any(PublicLink.class));
    }
}
