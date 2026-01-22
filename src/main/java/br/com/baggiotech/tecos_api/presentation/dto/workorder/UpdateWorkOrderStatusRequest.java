package br.com.baggiotech.tecos_api.presentation.dto.workorder;

import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkOrderStatusRequest(
        @NotNull(message = "Status é obrigatório")
        OrderStatus status
) {
}
