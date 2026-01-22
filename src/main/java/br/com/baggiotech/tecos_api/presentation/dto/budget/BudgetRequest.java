package br.com.baggiotech.tecos_api.presentation.dto.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record BudgetRequest(
        @NotNull(message = "Company ID é obrigatório")
        UUID companyId,
        
        @NotNull(message = "Work Order ID é obrigatório")
        UUID workOrderId,
        
        @NotNull(message = "Valor do serviço é obrigatório")
        @DecimalMin(value = "0.0", message = "Valor do serviço deve ser maior ou igual a zero")
        BigDecimal serviceValue,
        
        @DecimalMin(value = "0.0", message = "Valor das peças deve ser maior ou igual a zero")
        BigDecimal partsValue,
        
        @NotNull(message = "Valor total é obrigatório")
        @DecimalMin(value = "0.0", message = "Valor total deve ser maior ou igual a zero")
        BigDecimal totalValue,
        
        @NotNull(message = "User ID (criador) é obrigatório")
        UUID createdBy
) {
}
