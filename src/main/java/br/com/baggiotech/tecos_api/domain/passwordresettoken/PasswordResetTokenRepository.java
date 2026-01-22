package br.com.baggiotech.tecos_api.domain.passwordresettoken;

import java.util.Optional;

public interface PasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByEmail(String email);
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByEmail(String email);
    void deleteByToken(String token);
    boolean existsByEmail(String email);
    boolean existsByToken(String token);
}
