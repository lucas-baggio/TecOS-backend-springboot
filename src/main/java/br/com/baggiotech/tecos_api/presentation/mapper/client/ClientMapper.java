package br.com.baggiotech.tecos_api.presentation.mapper.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.presentation.dto.client.ClientResponse;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    
    public ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getCompany() != null ? client.getCompany().getId() : null,
                client.getCompany() != null ? client.getCompany().getName() : null,
                client.getName(),
                client.getPhone(),
                client.getEmail(),
                client.getCpf(),
                client.getObservations(),
                client.getIsActive(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}
