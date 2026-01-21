package br.com.baggiotech.tecos_api.infrastructure.persistence.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository jpaRepository;

    public CompanyRepositoryImpl(CompanyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Company save(Company company) {
        CompanyJpaEntity entity = toJpaEntity(company);
        if (company.getId() != null && !jpaRepository.existsById(company.getId())) {
            entity.setId(null);
        }
        CompanyJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Company> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<Company> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, UUID id) {
        return jpaRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public List<Company> findByIsActive(Boolean isActive) {
        return jpaRepository.findByIsActive(isActive).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Company> searchByNameOrEmail(String search) {
        return jpaRepository.searchByNameOrEmail(search).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CompanyJpaEntity toJpaEntity(Company company) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setId(company.getId());
        entity.setName(company.getName());
        entity.setEmail(company.getEmail());
        entity.setWhatsapp(company.getWhatsapp());
        entity.setLogoUrl(company.getLogoUrl());
        entity.setIsActive(company.getIsActive());
        entity.setCreatedAt(company.getCreatedAt());
        entity.setUpdatedAt(company.getUpdatedAt());
        return entity;
    }

    private Company toDomain(CompanyJpaEntity entity) {
        return new Company(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getWhatsapp(),
                entity.getLogoUrl(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
