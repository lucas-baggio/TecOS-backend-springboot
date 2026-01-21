package br.com.baggiotech.tecos_api.presentation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token é obrigatório")
        String token,
        
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,
        
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String password,
        
        @NotBlank(message = "Confirmação de senha é obrigatória")
        String passwordConfirmation
) {
}
