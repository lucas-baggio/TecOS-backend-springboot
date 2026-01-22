package br.com.baggiotech.tecos_api.domain.publiclink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublicLinkRepository {
    PublicLink save(PublicLink publicLink);
    Optional<PublicLink> findById(UUID id);
    boolean existsById(UUID id);
    void delete(PublicLink publicLink);
    List<PublicLink> findAll();
    List<PublicLink> findByWorkOrderId(UUID workOrderId);
    Optional<PublicLink> findByToken(String token);
    boolean existsByToken(String token);
}
