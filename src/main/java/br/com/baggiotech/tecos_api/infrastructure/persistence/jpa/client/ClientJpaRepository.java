package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientJpaRepository extends JpaRepository<ClientJpaEntity, UUID> {
    List<ClientJpaEntity> findByCompanyId(UUID companyId);
    List<ClientJpaEntity> findByIsActive(Boolean isActive);
    
    @Query("SELECT c FROM ClientJpaEntity c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.cpf) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<ClientJpaEntity> searchByNameOrEmailOrCpfOrPhone(@Param("search") String search);
    
    @Query("SELECT c FROM ClientJpaEntity c WHERE c.company.id = :companyId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.cpf) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ClientJpaEntity> findByCompanyIdAndSearch(@Param("companyId") UUID companyId, @Param("search") String search);
    
    @Query("SELECT c FROM ClientJpaEntity c WHERE c.company.id = :companyId AND c.isActive = :isActive")
    List<ClientJpaEntity> findByCompanyIdAndIsActive(@Param("companyId") UUID companyId, @Param("isActive") Boolean isActive);
}
