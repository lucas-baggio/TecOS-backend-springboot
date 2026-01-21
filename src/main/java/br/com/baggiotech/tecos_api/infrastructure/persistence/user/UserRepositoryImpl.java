package br.com.baggiotech.tecos_api.infrastructure.persistence.user;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toJpaEntity(user);
        if (user.getId() != null && !jpaRepository.existsById(user.getId())) {
            entity.setId(null);
        }
        UserJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
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
    public Optional<User> findByEmail(String email) {
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
    public Optional<User> findByEmailAndCompanyId(String email, UUID companyId) {
        return jpaRepository.findByEmailAndCompanyId(email, companyId)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmailAndCompanyId(String email, UUID companyId) {
        return jpaRepository.existsByEmailAndCompanyId(email, companyId);
    }

    @Override
    public boolean existsByEmailAndCompanyIdAndIdNot(String email, UUID companyId, UUID id) {
        return jpaRepository.existsByEmailAndCompanyIdAndIdNot(email, companyId, id);
    }

    @Override
    public List<User> findByCompanyId(UUID companyId) {
        return jpaRepository.findByCompanyId(companyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByIsActive(Boolean isActive) {
        return jpaRepository.findByIsActive(isActive).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByType(String type) {
        return jpaRepository.findByType(type).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> searchByNameOrEmail(String search) {
        return jpaRepository.searchByNameOrEmail(search).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private UserJpaEntity toJpaEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        
        if (user.getCompany() != null) {
            CompanyJpaEntity companyEntity = new CompanyJpaEntity();
            companyEntity.setId(user.getCompany().getId());
            entity.setCompany(companyEntity);
        }
        
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setType(user.getType());
        entity.setIsActive(user.getIsActive());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setDeletedAt(user.getDeletedAt());
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        Company company = null;
        if (entity.getCompany() != null) {
            company = new Company(
                    entity.getCompany().getId(),
                    entity.getCompany().getName(),
                    entity.getCompany().getEmail(),
                    entity.getCompany().getWhatsapp(),
                    entity.getCompany().getLogoUrl(),
                    entity.getCompany().getIsActive(),
                    entity.getCompany().getCreatedAt(),
                    entity.getCompany().getUpdatedAt()
            );
        }
        
        return new User(
                entity.getId(),
                company,
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getType(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
