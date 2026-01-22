package br.com.baggiotech.tecos_api.infrastructure.persistence.passwordresettoken;

import br.com.baggiotech.tecos_api.domain.passwordresettoken.PasswordResetToken;
import br.com.baggiotech.tecos_api.domain.passwordresettoken.PasswordResetTokenRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.passwordresettoken.PasswordResetTokenJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.passwordresettoken.PasswordResetTokenJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryImpl(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenJpaEntity entity = toJpaEntity(token);
        PasswordResetTokenJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<PasswordResetToken> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(this::toDomain);
    }

    @Override
    public void deleteByEmail(String email) {
        jpaRepository.deleteByEmail(email);
    }

    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteByToken(token);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByToken(String token) {
        return jpaRepository.existsByToken(token);
    }

    private PasswordResetTokenJpaEntity toJpaEntity(PasswordResetToken token) {
        PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity();
        entity.setEmail(token.getEmail());
        entity.setToken(token.getToken());
        entity.setCreatedAt(token.getCreatedAt());
        return entity;
    }

    private PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(entity.getEmail());
        token.setToken(entity.getToken());
        token.setCreatedAt(entity.getCreatedAt());
        return token;
    }
}
