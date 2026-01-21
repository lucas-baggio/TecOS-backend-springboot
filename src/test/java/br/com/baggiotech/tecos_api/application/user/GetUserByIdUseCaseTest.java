package br.com.baggiotech.tecos_api.application.user;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserByIdUseCase Tests")
class GetUserByIdUseCaseTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private GetUserByIdUseCase useCase;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setPassword("password123");
        user.setType("TECNICO");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void shouldGetUserByIdSuccessfully() {
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        
        User result = useCase.execute(userId);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Test User");
        verify(repository).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> useCase.execute(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining(nonExistentId.toString());

        verify(repository).findById(nonExistentId);
    }
}
