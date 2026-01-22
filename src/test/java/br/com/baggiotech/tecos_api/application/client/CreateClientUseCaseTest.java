package br.com.baggiotech.tecos_api.application.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
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
@DisplayName("CreateClientUseCase Tests")
class CreateClientUseCaseTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private CreateClientUseCase useCase;

    private Company company;
    private Client savedClient;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Test Company");
        company.setEmail("company@example.com");
        company.setIsActive(true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        savedClient = new Client();
        savedClient.setId(UUID.randomUUID());
        savedClient.setCompany(company);
        savedClient.setName("Test Client");
        savedClient.setPhone("11999999999");
        savedClient.setEmail("client@example.com");
        savedClient.setCpf("123.456.789-00");
        savedClient.setIsActive(true);
        savedClient.setCreatedAt(LocalDateTime.now());
        savedClient.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar um cliente com sucesso")
    void shouldCreateClientSuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(companyRepository.findById(any(UUID.class))).thenReturn(Optional.of(company));
        when(repository.save(any(Client.class))).thenReturn(savedClient);
        when(metrics.startTimer()).thenReturn(sample);
        
        Client result = useCase.execute(
                company.getId(),
                "Test Client",
                "11999999999",
                "client@example.com",
                "123.456.789-00",
                "Observations",
                true
        );
        
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Client");
        assertThat(result.getPhone()).isEqualTo("11999999999");
        assertThat(result.getEmail()).isEqualTo("client@example.com");
        assertThat(result.getCpf()).isEqualTo("123.456.789-00");
        assertThat(result.getIsActive()).isTrue();

        verify(companyRepository).findById(company.getId());
        verify(repository).save(any(Client.class));
        verify(metrics).incrementClientsCreated();
    }

    @Test
    @DisplayName("Deve criar cliente com isActive padrão como true quando não informado")
    void shouldCreateClientWithDefaultIsActive() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(companyRepository.findById(any(UUID.class))).thenReturn(Optional.of(company));
        when(repository.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            assertThat(client.getIsActive()).isTrue();
            return savedClient;
        });
        when(metrics.startTimer()).thenReturn(sample);
        
        useCase.execute(company.getId(), "Test Client", "11999999999", null, null, null, null);
        verify(repository).save(any(Client.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando company não existe")
    void shouldThrowExceptionWhenCompanyNotFound() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        UUID nonExistentCompanyId = UUID.randomUUID();
        when(companyRepository.findById(nonExistentCompanyId)).thenReturn(Optional.empty());
        when(metrics.startTimer()).thenReturn(sample);
        
        assertThatThrownBy(() -> useCase.execute(
                nonExistentCompanyId,
                "Test Client",
                "11999999999",
                null,
                null,
                null,
                true
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Company");

        verify(companyRepository).findById(nonExistentCompanyId);
        verify(repository, never()).save(any(Client.class));
    }
}
