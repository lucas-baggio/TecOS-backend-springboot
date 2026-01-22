package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeletePublicLinkUseCase {

    private final PublicLinkRepository repository;

    public DeletePublicLinkUseCase(PublicLinkRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID id, UUID companyId) {
        PublicLink publicLink = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PublicLink", id));

        // Verificar se pertence à mesma company (através do workOrder)
        if (publicLink.getWorkOrder() == null ||
            publicLink.getWorkOrder().getCompany() == null ||
            !publicLink.getWorkOrder().getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Acesso negado.");
        }

        repository.delete(publicLink);
    }
}
