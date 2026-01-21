package br.com.baggiotech.tecos_api.application.user;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
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
@DisplayName("UpdateUserProfileUseCase Tests")
class UpdateUserProfileUseCaseTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UpdateUserProfileUseCase useCase;

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

        existingUser = new User();
        existingUser.setId(userId);
        existingUser.setCompany(company);
        existingUser.setName("Original Name");
        existingUser.setEmail("original@example.com");
        existingUser.setCreatedAt(LocalDateTime.now());
        existingUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve atualizar perfil com sucesso")
    void shouldUpdateProfileSuccessfully() {
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.existsByEmailAndCompanyIdAndIdNot(anyString(), any(UUID.class), any(UUID.class))).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = useCase.execute(
                userId,
                "Updated Name",
                "updated@example.com"
        );

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");

        verify(repository).findById(userId);
        verify(repository).existsByEmailAndCompanyIdAndIdNot("updated@example.com", companyId, userId);
        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve atualizar apenas nome quando fornecido")
    void shouldUpdateOnlyNameWhenProvided() {
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = useCase.execute(userId, "Updated Name", null);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("original@example.com");
        verify(repository, never()).existsByEmailAndCompanyIdAndIdNot(anyString(), any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("Deve atualizar apenas email quando fornecido")
    void shouldUpdateOnlyEmailWhenProvided() {
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.existsByEmailAndCompanyIdAndIdNot(anyString(), any(UUID.class), any(UUID.class))).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = useCase.execute(userId, null, "updated@example.com");

        assertThat(result.getName()).isEqualTo("Original Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        verify(repository).existsByEmailAndCompanyIdAndIdNot("updated@example.com", companyId, userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId, "Updated Name", null))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe na company")
    void shouldThrowExceptionWhenEmailExistsInCompany() {
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.existsByEmailAndCompanyIdAndIdNot("existing@example.com", companyId, userId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(userId, null, "existing@example.com"))
                .isInstanceOf(EntityAlreadyExistsException.class);

        verify(repository).existsByEmailAndCompanyIdAndIdNot("existing@example.com", companyId, userId);
        verify(repository, never()).save(any(User.class));
    }
}
