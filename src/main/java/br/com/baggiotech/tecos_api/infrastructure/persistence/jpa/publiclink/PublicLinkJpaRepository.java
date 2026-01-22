package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.publiclink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublicLinkJpaRepository extends JpaRepository<PublicLinkJpaEntity, UUID> {
    
    @Query("SELECT p FROM PublicLinkJpaEntity p WHERE p.workOrder.id = :workOrderId")
    List<PublicLinkJpaEntity> findByWorkOrderId(@Param("workOrderId") UUID workOrderId);
    
    Optional<PublicLinkJpaEntity> findByToken(String token);
    
    boolean existsByToken(String token);
}
