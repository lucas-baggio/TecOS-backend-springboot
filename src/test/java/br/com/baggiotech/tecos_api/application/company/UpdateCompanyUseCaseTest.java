package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
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
@DisplayName("UpdateCompanyUseCase Tests")
class UpdateCompanyUseCaseTest {

    @Mock
    private CompanyRepository repository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private UpdateCompanyUseCase useCase;

    private Company existingCompany;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        existingCompany = new Company();
        existingCompany.setId(companyId);
        existingCompany.setName("Original Name");
        existingCompany.setEmail("original@example.com");
        existingCompany.setWhatsapp("11999999999");
        existingCompany.setLogoUrl("https://example.com/logo.png");
        existingCompany.setIsActive(true);
        existingCompany.setCreatedAt(LocalDateTime.now());
        existingCompany.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve atualizar empresa com sucesso")
    void shouldUpdateCompanySuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(repository.existsByEmailAndIdNot(anyString(), any(UUID.class))).thenReturn(false);
        when(repository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.startTimer()).thenReturn(sample);
        
        Company result = useCase.execute(
                companyId,
                "Updated Name",
                "updated@example.com",
                "11888888888",
                "https://example.com/new-logo.png",
                false
        );
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getWhatsapp()).isEqualTo("11888888888");
        assertThat(result.getLogoUrl()).isEqualTo("https://example.com/new-logo.png");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(repository).findById(companyId);
        verify(repository).existsByEmailAndIdNot("updated@example.com", companyId);
        verify(repository).save(any(Company.class));
    }

    @Test
    @DisplayName("Deve atualizar apenas campos fornecidos")
    void shouldUpdateOnlyProvidedFields() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(repository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.startTimer()).thenReturn(sample);
        
        Company result = useCase.execute(
                companyId,
                "Updated Name",
                null,
                null,
                null,
                null
        );
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("original@example.com");
        assertThat(result.getWhatsapp()).isEqualTo("11999999999");
        assertThat(result.getLogoUrl()).isEqualTo("https://example.com/logo.png");
        assertThat(result.getIsActive()).isTrue();

        verify(repository, never()).existsByEmailAndIdNot(anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando empresa não encontrada")
    void shouldThrowExceptionWhenCompanyNotFound() {
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
                null
        ))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe em outra empresa")
    void shouldThrowExceptionWhenEmailExistsInAnotherCompany() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(repository.existsByEmailAndIdNot("existing@example.com", companyId)).thenReturn(true);
        when(metrics.startTimer()).thenReturn(sample);
        
        assertThatThrownBy(() -> useCase.execute(
                companyId,
                null,
                "existing@example.com",
                null,
                null,
                null
        ))
                .isInstanceOf(EntityAlreadyExistsException.class);

        verify(repository).existsByEmailAndIdNot("existing@example.com", companyId);
        verify(repository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("Não deve validar email quando é o mesmo da empresa")
    void shouldNotValidateEmailWhenSameAsExisting() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(repository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.startTimer()).thenReturn(sample);
        
        useCase.execute(
                companyId,
                null,
                "original@example.com",
                null,
                null,
                null
        );
        verify(repository, never()).existsByEmailAndIdNot(anyString(), any(UUID.class));
        verify(repository).save(any(Company.class));
    }
}
