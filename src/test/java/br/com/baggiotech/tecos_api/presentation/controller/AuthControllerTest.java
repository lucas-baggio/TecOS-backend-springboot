package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.auth.ChangePasswordUseCase;
import br.com.baggiotech.tecos_api.application.auth.LoginUseCase;
import br.com.baggiotech.tecos_api.application.user.GetUserByIdUseCase;
import br.com.baggiotech.tecos_api.application.user.UpdateUserProfileUseCase;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.presentation.exception.GlobalExceptionHandler;
import br.com.baggiotech.tecos_api.presentation.mapper.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private GetUserByIdUseCase getUserByIdUseCase;

    @Mock
    private ChangePasswordUseCase changePasswordUseCase;

    @Mock
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @Mock
    private UserMapper userMapper;

    private AuthController controller;

    private User user;
    private Company company;
    private UUID userId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
                loginUseCase,
                getUserByIdUseCase,
                changePasswordUseCase,
                updateUserProfileUseCase,
                userMapper
        );

        org.springframework.validation.beanvalidation.LocalValidatorFactoryBean validator =
                new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();

        company = new Company();
        company.setId(companyId);
        company.setName("Test Company");
        company.setEmail("company@example.com");
        company.setIsActive(true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        user = new User();
        user.setId(userId);
        user.setCompany(company);
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setPassword("$2a$10$encodedPassword");
        user.setType("TECNICO");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        String requestBody = """
                {
                    "email": "user@example.com",
                    "password": "password123"
                }
                """;

        when(loginUseCase.execute("user@example.com", "password123")).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                    u.getId(), companyId, company.getName(), u.getName(), u.getEmail(),
                    u.getType(), u.getIsActive(), u.getCreatedAt(), u.getUpdatedAt()
            );
        });

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(loginUseCase).execute("user@example.com", "password123");
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve retornar erro quando credenciais inválidas")
    void shouldReturnErrorWhenInvalidCredentials() throws Exception {
        String requestBody = """
                {
                    "email": "user@example.com",
                    "password": "wrongPassword"
                }
                """;

        when(loginUseCase.execute("user@example.com", "wrongPassword"))
                .thenThrow(new IllegalArgumentException("Credenciais inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/logout - Deve fazer logout com sucesso")
    void shouldLogoutSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout realizado com sucesso."));
    }

    @Test
    @DisplayName("GET /api/auth/me - Deve retornar usuário autenticado")
    void shouldGetAuthenticatedUser() throws Exception {
        when(getUserByIdUseCase.execute(userId)).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                    u.getId(), companyId, company.getName(), u.getName(), u.getEmail(),
                    u.getType(), u.getIsActive(), u.getCreatedAt(), u.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/auth/me")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(getUserByIdUseCase).execute(userId);
    }

    @Test
    @DisplayName("GET /api/auth/me - Deve retornar 401 quando não autenticado")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - Deve enviar link de recuperação")
    void shouldSendPasswordResetLink() throws Exception {
        String requestBody = """
                {
                    "email": "user@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("link de recuperação")));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Deve resetar senha com sucesso")
    void shouldResetPasswordSuccessfully() throws Exception {
        String requestBody = """
                {
                    "token": "reset-token",
                    "email": "user@example.com",
                    "password": "newPassword123",
                    "passwordConfirmation": "newPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso."));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Deve retornar erro quando senhas não coincidem")
    void shouldReturnErrorWhenPasswordsDoNotMatch() throws Exception {
        String requestBody = """
                {
                    "token": "reset-token",
                    "email": "user@example.com",
                    "password": "newPassword123",
                    "passwordConfirmation": "differentPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/auth/profile - Deve atualizar perfil com sucesso")
    void shouldUpdateProfileSuccessfully() throws Exception {
        String requestBody = """
                {
                    "name": "Updated Name",
                    "email": "updated@example.com"
                }
                """;

        user.setName("Updated Name");
        user.setEmail("updated@example.com");

        when(updateUserProfileUseCase.execute(userId, "Updated Name", "updated@example.com")).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                    u.getId(), companyId, company.getName(), u.getName(), u.getEmail(),
                    u.getType(), u.getIsActive(), u.getCreatedAt(), u.getUpdatedAt()
            );
        });

        mockMvc.perform(put("/api/auth/profile")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(updateUserProfileUseCase).execute(userId, "Updated Name", "updated@example.com");
    }

    @Test
    @DisplayName("PUT /api/auth/change-password - Deve alterar senha com sucesso")
    void shouldChangePasswordSuccessfully() throws Exception {
        String requestBody = """
                {
                    "currentPassword": "oldPassword123",
                    "password": "newPassword123",
                    "passwordConfirmation": "newPassword123"
                }
                """;

        doNothing().when(changePasswordUseCase).execute(userId, "oldPassword123", "newPassword123");

        mockMvc.perform(put("/api/auth/change-password")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha alterada com sucesso."));

        verify(changePasswordUseCase).execute(userId, "oldPassword123", "newPassword123");
    }

    @Test
    @DisplayName("PUT /api/auth/change-password - Deve retornar erro quando senhas não coincidem")
    void shouldReturnErrorWhenChangePasswordPasswordsDoNotMatch() throws Exception {
        String requestBody = """
                {
                    "currentPassword": "oldPassword123",
                    "password": "newPassword123",
                    "passwordConfirmation": "differentPassword"
                }
                """;

        mockMvc.perform(put("/api/auth/change-password")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(changePasswordUseCase, never()).execute(any(), any(), any());
    }
}
