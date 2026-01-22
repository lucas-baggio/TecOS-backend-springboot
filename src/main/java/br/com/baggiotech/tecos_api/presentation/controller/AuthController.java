package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.auth.ChangePasswordUseCase;
import br.com.baggiotech.tecos_api.application.auth.LoginUseCase;
import br.com.baggiotech.tecos_api.application.user.GetUserByIdUseCase;
import br.com.baggiotech.tecos_api.application.user.UpdateUserProfileUseCase;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.infrastructure.security.SecurityContext;
import br.com.baggiotech.tecos_api.presentation.dto.auth.ChangePasswordRequest;
import br.com.baggiotech.tecos_api.presentation.dto.auth.ForgotPasswordRequest;
import br.com.baggiotech.tecos_api.presentation.dto.auth.LoginRequest;
import br.com.baggiotech.tecos_api.presentation.dto.auth.LoginResponse;
import br.com.baggiotech.tecos_api.presentation.dto.auth.ResetPasswordRequest;
import br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse;
import br.com.baggiotech.tecos_api.presentation.mapper.user.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final UserMapper userMapper;

    public AuthController(LoginUseCase loginUseCase, GetUserByIdUseCase getUserByIdUseCase,
                         ChangePasswordUseCase changePasswordUseCase, 
                         UpdateUserProfileUseCase updateUserProfileUseCase,
                         UserMapper userMapper) {
        this.loginUseCase = loginUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = loginUseCase.execute(request.email(), request.password());
        
        // Definir o contexto de segurança para habilitar os filtros de company_id
        if (user.getCompany() != null) {
            SecurityContext.setCurrentCompanyId(user.getCompany().getId());
            SecurityContext.setCurrentUserId(user.getId());
        }
        
        UserResponse userResponse = userMapper.toResponse(user);
        String token = "mock-token-" + user.getId();
        
        LoginResponse response = new LoginResponse(userResponse, token, "Bearer");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Limpar o contexto de segurança
        SecurityContext.clear();
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso."));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            UUID userId = UUID.fromString(userIdHeader);
            User user = getUserByIdUseCase.execute(userId);
            UserResponse response = userMapper.toResponse(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(Map.of(
                "message", 
                "Se o email existir, um link de recuperação será enviado."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.password().equals(request.passwordConfirmation())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }
        
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @Valid @RequestBody Map<String, String> request) {
        
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            UUID userId = UUID.fromString(userIdHeader);
            User user = updateUserProfileUseCase.execute(
                    userId,
                    request.get("name"),
                    request.get("email")
            );
            
            UserResponse response = userMapper.toResponse(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!request.password().equals(request.passwordConfirmation())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }
        
        try {
            UUID userId = UUID.fromString(userIdHeader);
            changePasswordUseCase.execute(userId, request.currentPassword(), request.password());
            return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
