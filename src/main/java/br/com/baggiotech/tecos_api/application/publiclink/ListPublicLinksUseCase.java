package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListPublicLinksUseCase {

    private final PublicLinkRepository repository;

    public ListPublicLinksUseCase(PublicLinkRepository repository) {
        this.repository = repository;
    }

    public List<PublicLink> execute(UUID workOrderId) {
        if (workOrderId != null) {
            return repository.findByWorkOrderId(workOrderId);
        }
        return repository.findAll();
    }
}
