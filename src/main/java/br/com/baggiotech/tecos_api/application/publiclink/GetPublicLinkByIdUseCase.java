package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetPublicLinkByIdUseCase {

    private final PublicLinkRepository repository;

    public GetPublicLinkByIdUseCase(PublicLinkRepository repository) {
        this.repository = repository;
    }

    public PublicLink execute(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PublicLink", id));
    }
}
