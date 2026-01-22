package br.com.baggiotech.tecos_api.presentation.dto.publiclink;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectBudgetByTokenRequest(
        @NotBlank(message = "Motivo da rejeição é obrigatório")
        @Size(min = 10, message = "O motivo da rejeição deve ter no mínimo 10 caracteres")
        String rejectionReason
) {
}
