package br.com.baggiotech.tecos_api.domain.company;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {
    Company save(Company company);
    Optional<Company> findById(UUID id);
    List<Company> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
    Optional<Company> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    List<Company> findByIsActive(Boolean isActive);
    List<Company> searchByNameOrEmail(String search);
}
