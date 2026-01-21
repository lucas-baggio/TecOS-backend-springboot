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
@DisplayName("UpdateUserUseCase Tests")
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository repository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CustomMetrics metrics;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserUseCase useCase;

    private User existingUser;
    private Company company;
    private UUID userId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        company = new Company();
        company.setId(companyId);
        company.setName("Test Company");
        company.setEmail("company@example.com");
        company.setIsActive(true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        existingUser = new User();
        existingUser.setId(userId);
        existingUser.setCompany(company);
        existingUser.setName("Original Name");
        existingUser.setEmail("original@example.com");
        existingUser.setPassword("oldPassword");
        existingUser.setType("TECNICO");
        existingUser.setIsActive(true);
        existingUser.setCreatedAt(LocalDateTime.now());
        existingUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldUpdateUserSuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.existsByEmailAndCompanyIdAndIdNot(anyString(), any(UUID.class), any(UUID.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newEncodedPassword");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.startTimer()).thenReturn(sample);

        User result = useCase.execute(
                userId,
                companyId,
                "Updated Name",
                "updated@example.com",
                "newPassword",
                "ADMIN",
                false
        );

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getType()).isEqualTo("ADMIN");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getPassword()).isEqualTo("$2a$10$newEncodedPassword");

        verify(repository).findById(userId);
        verify(repository).existsByEmailAndCompanyIdAndIdNot("updated@example.com", companyId, userId);
        verify(passwordEncoder).encode("newPassword");
        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve atualizar apenas campos fornecidos")
    void shouldUpdateOnlyProvidedFields() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.startTimer()).thenReturn(sample);

        User result = useCase.execute(
                userId,
                null,
                "Updated Name",
                null,
                null,
                null,
                null
        );

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("original@example.com");
        assertThat(result.getType()).isEqualTo("TECNICO");
        assertThat(result.getIsActive()).isTrue();

        verify(repository, never()).existsByEmailAndCompanyIdAndIdNot(anyString(), any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                nonExistentId,
                null,
                "Updated Name",
                null,
                null,
                null,
                null
        ))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe na company")
    void shouldThrowExceptionWhenEmailExistsInCompany() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.existsByEmailAndCompanyIdAndIdNot("existing@example.com", companyId, userId)).thenReturn(true);
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                userId,
                null,
                null,
                "existing@example.com",
                null,
                null,
                null
        ))
                .isInstanceOf(EntityAlreadyExistsException.class);

        verify(repository).existsByEmailAndCompanyIdAndIdNot("existing@example.com", companyId, userId);
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve atualizar company quando fornecido")
    void shouldUpdateCompanyWhenProvided() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        Company newCompany = new Company();
        UUID newCompanyId = UUID.randomUUID();
        newCompany.setId(newCompanyId);
        newCompany.setName("New Company");

        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(companyRepository.findById(newCompanyId)).thenReturn(Optional.of(newCompany));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.startTimer()).thenReturn(sample);

        User result = useCase.execute(
                userId,
                newCompanyId,
                null,
                null,
                null,
                null,
                null
        );

        assertThat(result.getCompany().getId()).isEqualTo(newCompanyId);
        verify(companyRepository).findById(newCompanyId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo é inválido")
    void shouldThrowExceptionWhenTypeIsInvalid() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        io.micrometer.core.instrument.Timer.Sample sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
        
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(metrics.startTimer()).thenReturn(sample);

        assertThatThrownBy(() -> useCase.execute(
                userId,
                null,
                null,
                null,
                null,
                "INVALID",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo deve ser ADMIN ou TECNICO");

        verify(repository, never()).save(any(User.class));
    }
}
