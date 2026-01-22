package br.com.baggiotech.tecos_api.infrastructure.persistence.client;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.client.ClientRepository;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientJpaRepository jpaRepository;

    public ClientRepositoryImpl(ClientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Client save(Client client) {
        ClientJpaEntity entity = toJpaEntity(client);
        if (client.getId() != null && !jpaRepository.existsById(client.getId())) {
            entity.setId(null);
        }
        ClientJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Client> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void delete(Client client) {
        ClientJpaEntity entity = toJpaEntity(client);
        jpaRepository.delete(entity);
    }

    @Override
    public List<Client> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Client> findByCompanyId(UUID companyId) {
        return jpaRepository.findByCompanyId(companyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Client> findByIsActive(Boolean isActive) {
        return jpaRepository.findByIsActive(isActive).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Client> findByCompanyIdAndIsActive(UUID companyId, Boolean isActive) {
        return jpaRepository.findByCompanyIdAndIsActive(companyId, isActive).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Client> searchByNameOrEmailOrCpfOrPhone(String search) {
        return jpaRepository.searchByNameOrEmailOrCpfOrPhone(search).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Client> findByCompanyIdAndSearch(UUID companyId, String search) {
        return jpaRepository.findByCompanyIdAndSearch(companyId, search).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ClientJpaEntity toJpaEntity(Client client) {
        ClientJpaEntity entity = new ClientJpaEntity();
        entity.setId(client.getId());
        
        if (client.getCompany() != null) {
            CompanyJpaEntity companyEntity = new CompanyJpaEntity();
            companyEntity.setId(client.getCompany().getId());
            entity.setCompany(companyEntity);
        }
        
        entity.setName(client.getName());
        entity.setPhone(client.getPhone());
        entity.setEmail(client.getEmail());
        entity.setCpf(client.getCpf());
        entity.setObservations(client.getObservations());
        entity.setIsActive(client.getIsActive());
        entity.setCreatedAt(client.getCreatedAt());
        entity.setUpdatedAt(client.getUpdatedAt());
        entity.setDeletedAt(client.getDeletedAt());
        return entity;
    }

    private Client toDomain(ClientJpaEntity entity) {
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
        
        return new Client(
                entity.getId(),
                company,
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getCpf(),
                entity.getObservations(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
