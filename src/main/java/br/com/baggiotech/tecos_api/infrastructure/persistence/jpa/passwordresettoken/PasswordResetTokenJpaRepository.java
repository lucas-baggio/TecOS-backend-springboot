package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.passwordresettoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, String> {
    Optional<PasswordResetTokenJpaEntity> findByEmail(String email);
    Optional<PasswordResetTokenJpaEntity> findByToken(String token);
    void deleteByEmail(String email);
    void deleteByToken(String token);
    boolean existsByEmail(String email);
    boolean existsByToken(String token);
}
