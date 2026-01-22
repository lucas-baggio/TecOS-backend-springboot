package br.com.baggiotech.tecos_api.presentation.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ClientRequest(
        @NotNull(message = "Company ID é obrigatório")
        UUID companyId,
        
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String name,
        
        @NotBlank(message = "Telefone é obrigatório")
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String phone,
        
        @Email(message = "Email inválido")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,
        
        @Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
        @Pattern(regexp = "^$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$|^\\d{11}$", 
                message = "CPF deve estar no formato XXX.XXX.XXX-XX ou apenas números")
        String cpf,
        
        String observations,
        
        Boolean isActive
) {
}
