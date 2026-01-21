package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.user.*;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.presentation.dto.user.UserRequest;
import br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse;
import br.com.baggiotech.tecos_api.presentation.mapper.user.UserMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ListUsersUseCase listUsersUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserMapper mapper;

    public UserController(
            ListUsersUseCase listUsersUseCase,
            CreateUserUseCase createUserUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            UserMapper mapper) {
        this.listUsersUseCase = listUsersUseCase;
        this.createUserUseCase = createUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> index(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        
        Page<User> usersPage = listUsersUseCase.execute(page, size, companyId, isActive, type, search);
        Page<UserResponse> responsePage = usersPage.map(mapper::toResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping
    public ResponseEntity<UserResponse> store(@Valid @RequestBody UserRequest request) {
        User user = createUserUseCase.execute(
                request.companyId(),
                request.name(),
                request.email(),
                request.password(),
                request.type(),
                request.isActive()
        );
        
        UserResponse response = mapper.toResponse(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> show(@PathVariable UUID id) {
        User user = getUserByIdUseCase.execute(id);
        UserResponse response = mapper.toResponse(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequest request) {
        
        User user = updateUserUseCase.execute(
                id,
                request.companyId(),
                request.name(),
                request.email(),
                request.password(),
                request.type(),
                request.isActive()
        );
        
        UserResponse response = mapper.toResponse(user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> destroy(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        
        if (userIdHeader != null) {
            UUID currentUserId = UUID.fromString(userIdHeader);
            if (currentUserId.equals(id)) {
                throw new IllegalArgumentException("Não é possível excluir seu próprio usuário.");
            }
        }
        
        deleteUserUseCase.execute(id);
        return ResponseEntity.ok(Map.of("message", "Usuário excluído com sucesso."));
    }
}
