package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.user.*;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityAlreadyExistsException;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.presentation.exception.GlobalExceptionHandler;
import br.com.baggiotech.tecos_api.presentation.mapper.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ListUsersUseCase listUsersUseCase;

    @Mock
    private CreateUserUseCase createUserUseCase;

    @Mock
    private GetUserByIdUseCase getUserByIdUseCase;

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private DeleteUserUseCase deleteUserUseCase;

    @Mock
    private UserMapper mapper;

    private UserController controller;

    private User user;
    private Company company;
    private UUID userId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        controller = new UserController(
                listUsersUseCase,
                createUserUseCase,
                getUserByIdUseCase,
                updateUserUseCase,
                deleteUserUseCase,
                mapper
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
    @DisplayName("GET /api/users - Deve listar usuários com sucesso")
    void shouldListUsersSuccessfully() throws Exception {
        List<User> users = Arrays.asList(user);
        Pageable pageable = PageRequest.of(0, 15);
        Page<User> page = new PageImpl<>(users, pageable, users.size());
        when(listUsersUseCase.execute(any(), any(), any(), any(), any(), any())).thenReturn(page);

        br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse response =
                new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                        user.getId(), companyId, company.getName(), user.getName(), user.getEmail(),
                        user.getType(), user.getIsActive(), user.getCreatedAt(), user.getUpdatedAt()
                );
        when(mapper.toResponse(any(User.class))).thenReturn(response);

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test User"));

        verify(listUsersUseCase).execute(any(), any(), any(), any(), any(), any());
        verify(mapper, atLeastOnce()).toResponse(any(User.class));
    }

    @Test
    @DisplayName("GET /api/users - Deve filtrar por companyId")
    void shouldFilterByCompanyId() throws Exception {
        Pageable pageable = PageRequest.of(0, 15);
        Page<User> page = new PageImpl<>(Arrays.asList(user), pageable, 1);
        when(listUsersUseCase.execute(any(), any(), eq(companyId), any(), any(), any())).thenReturn(page);

        br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse response =
                new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                        user.getId(), companyId, company.getName(), user.getName(), user.getEmail(),
                        user.getType(), user.getIsActive(), user.getCreatedAt(), user.getUpdatedAt()
                );
        when(mapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                    u.getId(), companyId, company.getName(), u.getName(), u.getEmail(),
                    u.getType(), u.getIsActive(), u.getCreatedAt(), u.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/users")
                        .param("companyId", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(listUsersUseCase).execute(any(), any(), eq(companyId), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/users - Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() throws Exception {
        String requestBody = """
                {
                    "companyId": "%s",
                    "name": "New User",
                    "email": "new@example.com",
                    "password": "password123",
                    "type": "TECNICO",
                    "isActive": true
                }
                """.formatted(companyId);

        when(createUserUseCase.execute(any(UUID.class), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(user);
        when(mapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                    u.getId(), companyId, company.getName(), u.getName(), u.getEmail(),
                    u.getType(), u.getIsActive(), u.getCreatedAt(), u.getUpdatedAt()
            );
        });

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        verify(createUserUseCase).execute(
                eq(companyId),
                eq("New User"),
                eq("new@example.com"),
                eq("password123"),
                eq("TECNICO"),
                eq(true)
        );
    }

    @Test
    @DisplayName("POST /api/users - Deve retornar erro de validação quando nome está vazio")
    void shouldReturnValidationErrorWhenNameIsEmpty() throws Exception {
        String requestBody = """
                {
                    "companyId": "%s",
                    "name": "",
                    "email": "test@example.com",
                    "password": "password123",
                    "type": "TECNICO"
                }
                """.formatted(companyId);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("name"));

        verify(createUserUseCase, never()).execute(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/users - Deve retornar erro quando email já existe")
    void shouldReturnErrorWhenEmailAlreadyExists() throws Exception {
        String requestBody = """
                {
                    "companyId": "%s",
                    "name": "New User",
                    "email": "existing@example.com",
                    "password": "password123",
                    "type": "TECNICO"
                }
                """.formatted(companyId);

        when(createUserUseCase.execute(eq(companyId), eq("New User"), eq("existing@example.com"), eq("password123"), eq("TECNICO"), any()))
                .thenThrow(new EntityAlreadyExistsException("User", "email", "existing@example.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("já existe")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Deve buscar usuário por ID")
    void shouldGetUserById() throws Exception {
        when(getUserByIdUseCase.execute(userId)).thenReturn(user);
        when(mapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                    u.getId(), companyId, company.getName(), u.getName(), u.getEmail(),
                    u.getType(), u.getIsActive(), u.getCreatedAt(), u.getUpdatedAt()
            );
        });

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(getUserByIdUseCase).execute(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id} - Deve retornar 404 quando usuário não encontrado")
    void shouldReturn404WhenUserNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(getUserByIdUseCase.execute(nonExistentId))
                .thenThrow(new EntityNotFoundException("User", nonExistentId));

        mockMvc.perform(get("/api/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("não encontrado")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Deve atualizar usuário com sucesso")
    void shouldUpdateUserSuccessfully() throws Exception {
        String requestBody = """
                {
                    "companyId": "%s",
                    "name": "Updated User",
                    "email": "updated@example.com",
                    "password": "newPassword123",
                    "type": "ADMIN",
                    "isActive": false
                }
                """.formatted(companyId);

        user.setName("Updated User");
        user.setEmail("updated@example.com");
        user.setType("ADMIN");
        user.setIsActive(false);

        when(updateUserUseCase.execute(any(UUID.class), any(UUID.class), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(user);
        when(mapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse(
                    u.getId(), companyId, company.getName(), u.getName(), u.getEmail(),
                    u.getType(), u.getIsActive(), u.getCreatedAt(), u.getUpdatedAt()
            );
        });

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"));

        verify(updateUserUseCase).execute(
                eq(userId),
                eq(companyId),
                eq("Updated User"),
                eq("updated@example.com"),
                eq("newPassword123"),
                eq("ADMIN"),
                eq(false)
        );
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() throws Exception {
        doNothing().when(deleteUserUseCase).execute(userId);

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário excluído com sucesso."));

        verify(deleteUserUseCase).execute(userId);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Deve retornar erro quando tentar excluir próprio usuário")
    void shouldReturnErrorWhenDeletingOwnUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(deleteUserUseCase, never()).execute(userId);
    }
}
