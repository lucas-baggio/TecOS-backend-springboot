package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import org.springframework.stereotype.Service;

@Service
public class GetPublicLinkByTokenUseCase {

    private final PublicLinkRepository repository;

    public GetPublicLinkByTokenUseCase(PublicLinkRepository repository) {
        this.repository = repository;
    }

    public PublicLink execute(String token) {
        return repository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("PublicLink", "Token não encontrado"));
    }
}
