package br.com.baggiotech.tecos_api.presentation.dto.budget;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApproveBudgetRequest(
        @NotBlank(message = "Método de aprovação é obrigatório")
        String approvalMethod,
        
        @NotNull(message = "User ID (aprovador) é obrigatório")
        java.util.UUID approvedBy
) {
}
