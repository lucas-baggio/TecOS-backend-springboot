package br.com.baggiotech.tecos_api.application.auth;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase Tests")
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginUseCase useCase;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setPassword("$2a$10$encodedPassword");
        user.setType("TECNICO");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        User result = useCase.execute("user@example.com", "password123");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("nonexistent@example.com", "password123"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Credenciais inválidas");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha está incorreta")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("user@example.com", "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciais inválidas");

        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches("wrongPassword", "$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário está inativo")
    void shouldThrowExceptionWhenUserIsInactive() {
        user.setIsActive(false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("user@example.com", "password123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("desativada");

        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("Deve lançar exceção quando isActive é null")
    void shouldThrowExceptionWhenIsActiveIsNull() {
        user.setIsActive(null);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("user@example.com", "password123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("desativada");

        verify(userRepository).findByEmail("user@example.com");
    }
}
