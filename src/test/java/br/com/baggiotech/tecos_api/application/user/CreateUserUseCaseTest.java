package br.com.baggiotech.tecos_api.application.user;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import br.com.baggiotech.tecos_api.infrastructure.security.PasswordEncoder;
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
@DisplayName("CreateUserUseCase Tests")
class CreateUserUseCaseTest {

    @Mock
    private UserRepository repository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CustomMetrics metrics;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserUseCase useCase;

    private Company company;
    private User savedUser;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);
        company.setName("Test Company");
        company.setEmail("company@example.com");
        company.setIsActive(true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setCompany(company);
        savedUser.setName("Test User");
        savedUser.setEmail("user@example.com");
        savedUser.setPassword("$2a$10$encodedPassword");
        savedUser.setType("TECNICO");
        savedUser.setIsActive(true);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(repository.existsByEmailAndCompanyId(anyString(), any(UUID.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(repository.save(any(User.class))).thenReturn(savedUser);
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        when(metrics.startTimer()).thenReturn(sample);

        User result = useCase.execute(
                companyId,
                "Test User",
                "user@example.com",
                "password123",
                "TECNICO",
                true
        );

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getType()).isEqualTo("TECNICO");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getPassword()).isEqualTo("$2a$10$encodedPassword");

        verify(companyRepository).findById(companyId);
        verify(repository).existsByEmailAndCompanyId("user@example.com", companyId);
        verify(passwordEncoder).encode("password123");
        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve criar usuário com isActive padrão como true quando não informado")
    void shouldCreateUserWithDefaultIsActive() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(repository.existsByEmailAndCompanyId(anyString(), any(UUID.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getIsActive()).isTrue();
            return savedUser;
        });
        when(metrics.startTimer()).thenReturn(sample);

        useCase.execute(companyId, "Test User", "user@example.com", "password123", "TECNICO", null);

        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando company não existe")
    void shouldThrowExceptionWhenCompanyNotFound() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                companyId,
                "Test User",
                "user@example.com",
                "password123",
                "TECNICO",
                true
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Company");

        verify(companyRepository).findById(companyId);
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe na company")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(repository.existsByEmailAndCompanyId("existing@example.com", companyId)).thenReturn(true);
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                companyId,
                "Test User",
                "existing@example.com",
                "password123",
                "TECNICO",
                true
        ))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("email")
                .hasMessageContaining("existing@example.com");

        verify(repository).existsByEmailAndCompanyId("existing@example.com", companyId);
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo é inválido")
    void shouldThrowExceptionWhenTypeIsInvalid() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(repository.existsByEmailAndCompanyId(anyString(), any(UUID.class))).thenReturn(false);
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                companyId,
                "Test User",
                "user@example.com",
                "password123",
                "INVALID",
                true
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo deve ser ADMIN ou TECNICO");

        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve criar usuário do tipo ADMIN")
    void shouldCreateAdminUser() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(repository.existsByEmailAndCompanyId(anyString(), any(UUID.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        savedUser.setType("ADMIN");
        when(repository.save(any(User.class))).thenReturn(savedUser);
        when(metrics.startTimer()).thenReturn(sample);

        User result = useCase.execute(
                companyId,
                "Admin User",
                "admin@example.com",
                "password123",
                "ADMIN",
                true
        );

        assertThat(result.getType()).isEqualTo("ADMIN");
        verify(repository).save(any(User.class));
    }
}
