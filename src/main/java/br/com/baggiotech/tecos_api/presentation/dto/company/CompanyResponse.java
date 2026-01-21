package br.com.baggiotech.tecos_api.presentation.dto.company;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyResponse(
    UUID id,
    String name,
    String email,
    String whatsapp,
    String logoUrl,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
