package br.com.baggiotech.tecos_api.infrastructure.persistence.workorder;

import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WorkOrderRepositoryImpl implements WorkOrderRepository {

    private final WorkOrderJpaRepository jpaRepository;

    public WorkOrderRepositoryImpl(WorkOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WorkOrder save(WorkOrder workOrder) {
        WorkOrderJpaEntity entity = toJpaEntity(workOrder);
        if (workOrder.getId() != null && !jpaRepository.existsById(workOrder.getId())) {
            entity.setId(null);
        }
        WorkOrderJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<WorkOrder> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void delete(WorkOrder workOrder) {
        WorkOrderJpaEntity entity = toJpaEntity(workOrder);
        jpaRepository.delete(entity);
    }

    @Override
    public List<WorkOrder> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByCompanyId(UUID companyId) {
        return jpaRepository.findByCompanyId(companyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByClientId(UUID clientId) {
        return jpaRepository.findByClientId(clientId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByEquipmentId(UUID equipmentId) {
        return jpaRepository.findByEquipmentId(equipmentId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByTechnicianId(UUID technicianId) {
        return jpaRepository.findByTechnicianId(technicianId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByStatus(OrderStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByCompanyIdAndStatus(UUID companyId, OrderStatus status) {
        return jpaRepository.findByCompanyIdAndStatus(companyId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByClientIdAndStatus(UUID clientId, OrderStatus status) {
        return jpaRepository.findByClientIdAndStatus(clientId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByReturnOrder(Boolean returnOrder) {
        return jpaRepository.findByReturnOrder(returnOrder).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> searchByReportedDefectOrInternalObservationsOrClientName(String search) {
        return jpaRepository.searchByReportedDefectOrInternalObservationsOrClientName(search).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private WorkOrderJpaEntity toJpaEntity(WorkOrder workOrder) {
        WorkOrderJpaEntity entity = new WorkOrderJpaEntity();
        entity.setId(workOrder.getId());
        
        if (workOrder.getCompany() != null) {
            CompanyJpaEntity companyEntity = new CompanyJpaEntity();
            companyEntity.setId(workOrder.getCompany().getId());
            entity.setCompany(companyEntity);
        }
        
        if (workOrder.getClient() != null) {
            ClientJpaEntity clientEntity = new ClientJpaEntity();
            clientEntity.setId(workOrder.getClient().getId());
            entity.setClient(clientEntity);
        }
        
        if (workOrder.getEquipment() != null) {
            EquipmentJpaEntity equipmentEntity = new EquipmentJpaEntity();
            equipmentEntity.setId(workOrder.getEquipment().getId());
            entity.setEquipment(equipmentEntity);
        }
        
        if (workOrder.getTechnician() != null) {
            UserJpaEntity technicianEntity = new UserJpaEntity();
            technicianEntity.setId(workOrder.getTechnician().getId());
            entity.setTechnician(technicianEntity);
        }
        
        entity.setStatus(workOrder.getStatus());
        entity.setReportedDefect(workOrder.getReportedDefect());
        entity.setInternalObservations(workOrder.getInternalObservations());
        entity.setReturnOrder(workOrder.getReturnOrder());
        entity.setOriginWorkOrderId(workOrder.getOriginWorkOrderId());
        entity.setDeliveredAt(workOrder.getDeliveredAt());
        entity.setCreatedAt(workOrder.getCreatedAt());
        entity.setUpdatedAt(workOrder.getUpdatedAt());
        entity.setDeletedAt(workOrder.getDeletedAt());
        return entity;
    }

    private WorkOrder toDomain(WorkOrderJpaEntity entity) {
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
        
        Equipment equipment = null;
        if (entity.getEquipment() != null) {
            Company equipmentCompany = null;
            if (entity.getEquipment().getCompany() != null) {
                equipmentCompany = new Company(
                        entity.getEquipment().getCompany().getId(),
                        entity.getEquipment().getCompany().getName(),
                        entity.getEquipment().getCompany().getEmail(),
                        entity.getEquipment().getCompany().getWhatsapp(),
                        entity.getEquipment().getCompany().getLogoUrl(),
                        entity.getEquipment().getCompany().getIsActive(),
                        entity.getEquipment().getCompany().getCreatedAt(),
                        entity.getEquipment().getCompany().getUpdatedAt()
                );
            }
            
            Client equipmentClient = null;
            if (entity.getEquipment().getClient() != null) {
                Company equipmentClientCompany = null;
                if (entity.getEquipment().getClient().getCompany() != null) {
                    equipmentClientCompany = new Company(
                            entity.getEquipment().getClient().getCompany().getId(),
                            entity.getEquipment().getClient().getCompany().getName(),
                            entity.getEquipment().getClient().getCompany().getEmail(),
                            entity.getEquipment().getClient().getCompany().getWhatsapp(),
                            entity.getEquipment().getClient().getCompany().getLogoUrl(),
                            entity.getEquipment().getClient().getCompany().getIsActive(),
                            entity.getEquipment().getClient().getCompany().getCreatedAt(),
                            entity.getEquipment().getClient().getCompany().getUpdatedAt()
                    );
                }
                
                equipmentClient = new Client(
                        entity.getEquipment().getClient().getId(),
                        equipmentClientCompany,
                        entity.getEquipment().getClient().getName(),
                        entity.getEquipment().getClient().getPhone(),
                        entity.getEquipment().getClient().getEmail(),
                        entity.getEquipment().getClient().getCpf(),
                        entity.getEquipment().getClient().getObservations(),
                        entity.getEquipment().getClient().getIsActive(),
                        entity.getEquipment().getClient().getCreatedAt(),
                        entity.getEquipment().getClient().getUpdatedAt(),
                        entity.getEquipment().getClient().getDeletedAt()
                );
            }
            
            equipment = new Equipment(
                    entity.getEquipment().getId(),
                    equipmentCompany,
                    equipmentClient,
                    entity.getEquipment().getType(),
                    entity.getEquipment().getBrand(),
                    entity.getEquipment().getModel(),
                    entity.getEquipment().getSerialNumber(),
                    entity.getEquipment().getObservations(),
                    entity.getEquipment().getCreatedAt(),
                    entity.getEquipment().getUpdatedAt(),
                    entity.getEquipment().getDeletedAt()
            );
        }
        
        User technician = null;
        if (entity.getTechnician() != null) {
            Company technicianCompany = null;
            if (entity.getTechnician().getCompany() != null) {
                technicianCompany = new Company(
                        entity.getTechnician().getCompany().getId(),
                        entity.getTechnician().getCompany().getName(),
                        entity.getTechnician().getCompany().getEmail(),
                        entity.getTechnician().getCompany().getWhatsapp(),
                        entity.getTechnician().getCompany().getLogoUrl(),
                        entity.getTechnician().getCompany().getIsActive(),
                        entity.getTechnician().getCompany().getCreatedAt(),
                        entity.getTechnician().getCompany().getUpdatedAt()
                );
            }
            
            technician = new User(
                    entity.getTechnician().getId(),
                    technicianCompany,
                    entity.getTechnician().getName(),
                    entity.getTechnician().getEmail(),
                    entity.getTechnician().getPassword(),
                    entity.getTechnician().getType(),
                    entity.getTechnician().getIsActive(),
                    entity.getTechnician().getCreatedAt(),
                    entity.getTechnician().getUpdatedAt(),
                    entity.getTechnician().getDeletedAt()
            );
        }
        
        return new WorkOrder(
                entity.getId(),
                company,
                client,
                equipment,
                technician,
                entity.getStatus(),
                entity.getReportedDefect(),
                entity.getInternalObservations(),
                entity.getReturnOrder(),
                entity.getOriginWorkOrderId(),
                entity.getDeliveredAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
