package br.com.baggiotech.tecos_api.presentation.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID companyId,
        String companyName,
        String name,
        String email,
        String type,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
