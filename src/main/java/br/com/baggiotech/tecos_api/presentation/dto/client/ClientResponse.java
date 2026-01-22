package br.com.baggiotech.tecos_api.presentation.dto.client;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        UUID companyId,
        String companyName,
        String name,
        String phone,
        String email,
        String cpf,
        String observations,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
