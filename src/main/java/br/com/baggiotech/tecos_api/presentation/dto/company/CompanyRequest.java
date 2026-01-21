package br.com.baggiotech.tecos_api.presentation.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CompanyRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    String name,
    
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
    String email,
    
    @Size(max = 20, message = "WhatsApp deve ter no máximo 20 caracteres")
    String whatsapp,
    
    @URL(message = "Logo URL deve ser uma URL válida")
    @Size(max = 500, message = "Logo URL deve ter no máximo 500 caracteres")
    String logoUrl,
    
    Boolean isActive
) {
    public CompanyRequest {
        if (isActive == null) {
            isActive = true;
        }
    }
}
