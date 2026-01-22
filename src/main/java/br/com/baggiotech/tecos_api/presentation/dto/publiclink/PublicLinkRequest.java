package br.com.baggiotech.tecos_api.presentation.dto.publiclink;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PublicLinkRequest(
        @NotNull(message = "Work Order ID é obrigatório")
        UUID workOrderId
) {
}
