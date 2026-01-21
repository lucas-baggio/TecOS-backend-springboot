package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    List<UserJpaEntity> findByCompanyId(UUID companyId);
    List<UserJpaEntity> findByIsActive(Boolean isActive);
    List<UserJpaEntity> findByType(String type);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<UserJpaEntity> searchByNameOrEmail(@Param("search") String search);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.company.id = :companyId")
    Optional<UserJpaEntity> findByEmailAndCompanyId(@Param("email") String email, @Param("companyId") UUID companyId);
    
    @Query("SELECT COUNT(u) > 0 FROM UserJpaEntity u WHERE u.email = :email AND u.company.id = :companyId")
    boolean existsByEmailAndCompanyId(@Param("email") String email, @Param("companyId") UUID companyId);
    
    @Query("SELECT COUNT(u) > 0 FROM UserJpaEntity u WHERE u.email = :email AND u.company.id = :companyId AND u.id != :id")
    boolean existsByEmailAndCompanyIdAndIdNot(@Param("email") String email, @Param("companyId") UUID companyId, @Param("id") UUID id);
}
