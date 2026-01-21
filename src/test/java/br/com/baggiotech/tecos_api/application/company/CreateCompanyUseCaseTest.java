package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCompanyUseCase Tests")
class CreateCompanyUseCaseTest {

    @Mock
    private CompanyRepository repository;

    @Mock
    private CustomMetrics metrics;

    @InjectMocks
    private CreateCompanyUseCase useCase;

    private Company savedCompany;

    @BeforeEach
    void setUp() {
        savedCompany = new Company();
        savedCompany.setId(UUID.randomUUID());
        savedCompany.setName("Test Company");
        savedCompany.setEmail("test@example.com");
        savedCompany.setWhatsapp("11999999999");
        savedCompany.setLogoUrl("https://example.com/logo.png");
        savedCompany.setIsActive(true);
        savedCompany.setCreatedAt(LocalDateTime.now());
        savedCompany.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar uma empresa com sucesso")
    void shouldCreateCompanySuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.save(any(Company.class))).thenReturn(savedCompany);
        when(metrics.startTimer()).thenReturn(sample);
        
        Company result = useCase.execute(
                "Test Company",
                "test@example.com",
                "11999999999",
                "https://example.com/logo.png",
                true
        );
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Company");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getWhatsapp()).isEqualTo("11999999999");
        assertThat(result.getLogoUrl()).isEqualTo("https://example.com/logo.png");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(repository).existsByEmail("test@example.com");
        verify(repository).save(any(Company.class));
    }

    @Test
    @DisplayName("Deve criar empresa com isActive padrão como true quando não informado")
    void shouldCreateCompanyWithDefaultIsActive() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.save(any(Company.class))).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            assertThat(company.getIsActive()).isTrue();
            return savedCompany;
        });
        when(metrics.startTimer()).thenReturn(sample);
        
        useCase.execute("Test Company", "test@example.com", null, null, null);
        verify(repository).save(any(Company.class));
    }

    @Test
    @DisplayName("Deve criar empresa sem email")
    void shouldCreateCompanyWithoutEmail() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.save(any(Company.class))).thenReturn(savedCompany);
        when(metrics.startTimer()).thenReturn(sample);
        
        Company result = useCase.execute("Test Company", null, null, null, true);
        assertThat(result).isNotNull();
        verify(repository, never()).existsByEmail(anyString());
        verify(repository).save(any(Company.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.existsByEmail("existing@example.com")).thenReturn(true);
        when(metrics.startTimer()).thenReturn(sample);
        
        assertThatThrownBy(() -> useCase.execute(
                "Test Company",
                "existing@example.com",
                null,
                null,
                true
        ))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessageContaining("Company")
                .hasMessageContaining("email")
                .hasMessageContaining("existing@example.com");

        verify(repository).existsByEmail("existing@example.com");
        verify(repository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("Não deve validar email quando email está em branco")
    void shouldNotValidateEmailWhenBlank() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.save(any(Company.class))).thenReturn(savedCompany);
        when(metrics.startTimer()).thenReturn(sample);
        
        useCase.execute("Test Company", "   ", null, null, true);
        verify(repository, never()).existsByEmail(anyString());
        verify(repository).save(any(Company.class));
    }
}
