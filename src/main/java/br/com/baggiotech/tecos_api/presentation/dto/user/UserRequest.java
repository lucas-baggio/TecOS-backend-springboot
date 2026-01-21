package br.com.baggiotech.tecos_api.presentation.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UserRequest(
        @NotNull(message = "Company ID é obrigatório")
        UUID companyId,
        
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String name,
        
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,
        
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String password,
        
        @NotBlank(message = "Tipo é obrigatório")
        @Pattern(regexp = "ADMIN|TECNICO", message = "Tipo deve ser ADMIN ou TECNICO")
        String type,
        
        Boolean isActive
) {
}
