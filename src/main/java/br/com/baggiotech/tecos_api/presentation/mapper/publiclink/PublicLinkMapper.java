package br.com.baggiotech.tecos_api.presentation.mapper.publiclink;

import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.presentation.dto.publiclink.PublicLinkResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PublicLinkMapper {
    
    @Value("${app.public.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public PublicLinkResponse toResponse(PublicLink publicLink) {
        String publicUrl = baseUrl + "/api/public/" + publicLink.getToken();
        
        return new PublicLinkResponse(
                publicLink.getId(),
                publicLink.getWorkOrder() != null ? publicLink.getWorkOrder().getId() : null,
                publicLink.getToken(),
                publicUrl,
                publicLink.getCreatedAt(),
                publicLink.getUpdatedAt()
        );
    }
}
