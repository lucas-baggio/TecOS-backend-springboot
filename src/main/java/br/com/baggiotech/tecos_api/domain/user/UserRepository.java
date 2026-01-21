package br.com.baggiotech.tecos_api.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    List<User> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<User> findByEmailAndCompanyId(String email, UUID companyId);
    boolean existsByEmailAndCompanyId(String email, UUID companyId);
    boolean existsByEmailAndCompanyIdAndIdNot(String email, UUID companyId, UUID id);
    List<User> findByCompanyId(UUID companyId);
    List<User> findByIsActive(Boolean isActive);
    List<User> findByType(String type);
    List<User> searchByNameOrEmail(String search);
}
