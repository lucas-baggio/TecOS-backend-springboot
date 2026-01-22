package br.com.baggiotech.tecos_api.infrastructure.persistence.equipment;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.equipment.EquipmentRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EquipmentRepositoryImpl implements EquipmentRepository {

    private final EquipmentJpaRepository jpaRepository;

    public EquipmentRepositoryImpl(EquipmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Equipment save(Equipment equipment) {
        EquipmentJpaEntity entity = toJpaEntity(equipment);
        if (equipment.getId() != null && !jpaRepository.existsById(equipment.getId())) {
            entity.setId(null);
        }
        EquipmentJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Equipment> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void delete(Equipment equipment) {
        EquipmentJpaEntity entity = toJpaEntity(equipment);
        jpaRepository.delete(entity);
    }

    @Override
    public List<Equipment> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Equipment> findByCompanyId(UUID companyId) {
        return jpaRepository.findByCompanyId(companyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Equipment> findByClientId(UUID clientId) {
        return jpaRepository.findByClientId(clientId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Equipment> findByCompanyIdAndClientId(UUID companyId, UUID clientId) {
        return jpaRepository.findByCompanyIdAndClientId(companyId, clientId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Equipment> searchByTypeOrBrandOrModelOrSerialNumber(String search) {
        return jpaRepository.searchByTypeOrBrandOrModelOrSerialNumber(search).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Equipment> findByCompanyIdAndSearch(UUID companyId, String search) {
        return jpaRepository.findByCompanyIdAndSearch(companyId, search).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Equipment> findByClientIdAndSearch(UUID clientId, String search) {
        return jpaRepository.findByClientIdAndSearch(clientId, search).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByEquipmentId(UUID equipmentId) {
        return jpaRepository.countByEquipmentId(equipmentId);
    }

    private EquipmentJpaEntity toJpaEntity(Equipment equipment) {
        EquipmentJpaEntity entity = new EquipmentJpaEntity();
        entity.setId(equipment.getId());
        
        if (equipment.getCompany() != null) {
            CompanyJpaEntity companyEntity = new CompanyJpaEntity();
            companyEntity.setId(equipment.getCompany().getId());
            entity.setCompany(companyEntity);
        }
        
        if (equipment.getClient() != null) {
            ClientJpaEntity clientEntity = new ClientJpaEntity();
            clientEntity.setId(equipment.getClient().getId());
            entity.setClient(clientEntity);
        }
        
        entity.setType(equipment.getType());
        entity.setBrand(equipment.getBrand());
        entity.setModel(equipment.getModel());
        entity.setSerialNumber(equipment.getSerialNumber());
        entity.setObservations(equipment.getObservations());
        entity.setCreatedAt(equipment.getCreatedAt());
        entity.setUpdatedAt(equipment.getUpdatedAt());
        entity.setDeletedAt(equipment.getDeletedAt());
        return entity;
    }

    private Equipment toDomain(EquipmentJpaEntity entity) {
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
        
        Client client = null;
        if (entity.getClient() != null) {
            Company clientCompany = null;
            if (entity.getClient().getCompany() != null) {
                clientCompany = new Company(
                        entity.getClient().getCompany().getId(),
                        entity.getClient().getCompany().getName(),
                        entity.getClient().getCompany().getEmail(),
                        entity.getClient().getCompany().getWhatsapp(),
                        entity.getClient().getCompany().getLogoUrl(),
                        entity.getClient().getCompany().getIsActive(),
                        entity.getClient().getCompany().getCreatedAt(),
                        entity.getClient().getCompany().getUpdatedAt()
                );
            }
            
            client = new Client(
                    entity.getClient().getId(),
                    clientCompany,
                    entity.getClient().getName(),
                    entity.getClient().getPhone(),
                    entity.getClient().getEmail(),
                    entity.getClient().getCpf(),
                    entity.getClient().getObservations(),
                    entity.getClient().getIsActive(),
                    entity.getClient().getCreatedAt(),
                    entity.getClient().getUpdatedAt(),
                    entity.getClient().getDeletedAt()
            );
        }
        
        return new Equipment(
                entity.getId(),
                company,
                client,
                entity.getType(),
                entity.getBrand(),
                entity.getModel(),
                entity.getSerialNumber(),
                entity.getObservations(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
