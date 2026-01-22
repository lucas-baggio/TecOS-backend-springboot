package br.com.baggiotech.tecos_api.presentation.dto.publiclink;

import java.time.LocalDateTime;
import java.util.UUID;

public record PublicLinkResponse(
        UUID id,
        UUID workOrderId,
        String token,
        String publicUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
