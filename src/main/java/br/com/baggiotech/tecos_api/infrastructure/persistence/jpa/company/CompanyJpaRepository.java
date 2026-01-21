package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, UUID> {
    Optional<CompanyJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    List<CompanyJpaEntity> findByIsActive(Boolean isActive);
    
    @Query("SELECT c FROM CompanyJpaEntity c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<CompanyJpaEntity> searchByNameOrEmail(@Param("search") String search);
}
