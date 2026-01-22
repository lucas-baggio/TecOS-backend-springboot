package br.com.baggiotech.tecos_api.infrastructure.persistence.workorderhistory;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorderhistory.WorkOrderHistoryJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorderhistory.WorkOrderHistoryJpaRepository;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WorkOrderHistoryRepositoryImpl implements WorkOrderHistoryRepository {

    private final WorkOrderHistoryJpaRepository jpaRepository;
    private final WorkOrderRepository workOrderRepository;

    public WorkOrderHistoryRepositoryImpl(WorkOrderHistoryJpaRepository jpaRepository,
                                         WorkOrderRepository workOrderRepository) {
        this.jpaRepository = jpaRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Override
    public WorkOrderHistory save(WorkOrderHistory workOrderHistory) {
        WorkOrderHistoryJpaEntity entity = toJpaEntity(workOrderHistory);
        if (workOrderHistory.getId() != null && !jpaRepository.existsById(workOrderHistory.getId())) {
            entity.setId(null);
        }
        WorkOrderHistoryJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<WorkOrderHistory> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<WorkOrderHistory> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrderHistory> findByWorkOrderId(UUID workOrderId) {
        return jpaRepository.findByWorkOrderId(workOrderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrderHistory> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrderHistory> findByWorkOrderIdOrderByCreatedAtDesc(UUID workOrderId) {
        return jpaRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private WorkOrderHistoryJpaEntity toJpaEntity(WorkOrderHistory workOrderHistory) {
        WorkOrderHistoryJpaEntity entity = new WorkOrderHistoryJpaEntity();
        entity.setId(workOrderHistory.getId());
        
        if (workOrderHistory.getWorkOrder() != null) {
            WorkOrderJpaEntity workOrderEntity = new WorkOrderJpaEntity();
            workOrderEntity.setId(workOrderHistory.getWorkOrder().getId());
            entity.setWorkOrder(workOrderEntity);
        }
        
        if (workOrderHistory.getUser() != null) {
            UserJpaEntity userEntity = new UserJpaEntity();
            userEntity.setId(workOrderHistory.getUser().getId());
            entity.setUser(userEntity);
        }
        
        entity.setStatusBefore(workOrderHistory.getStatusBefore());
        entity.setStatusAfter(workOrderHistory.getStatusAfter());
        entity.setObservation(workOrderHistory.getObservation());
        entity.setCreatedAt(workOrderHistory.getCreatedAt());
        entity.setUpdatedAt(workOrderHistory.getUpdatedAt());
        return entity;
    }

    private WorkOrderHistory toDomain(WorkOrderHistoryJpaEntity entity) {
        WorkOrder workOrder = null;
        if (entity.getWorkOrder() != null) {
            // Carregar WorkOrder completo do repositório
            workOrder = workOrderRepository.findById(entity.getWorkOrder().getId())
                    .orElse(null);
            // Se não encontrar, criar um básico apenas com ID
            if (workOrder == null) {
                workOrder = new WorkOrder();
                workOrder.setId(entity.getWorkOrder().getId());
            }
        }
        
        User user = null;
        if (entity.getUser() != null) {
            Company userCompany = null;
            if (entity.getUser().getCompany() != null) {
                userCompany = new Company(
                        entity.getUser().getCompany().getId(),
                        entity.getUser().getCompany().getName(),
                        entity.getUser().getCompany().getEmail(),
                        entity.getUser().getCompany().getWhatsapp(),
                        entity.getUser().getCompany().getLogoUrl(),
                        entity.getUser().getCompany().getIsActive(),
                        entity.getUser().getCompany().getCreatedAt(),
                        entity.getUser().getCompany().getUpdatedAt()
                );
            }
            
            user = new User(
                    entity.getUser().getId(),
                    userCompany,
                    entity.getUser().getName(),
                    entity.getUser().getEmail(),
                    entity.getUser().getPassword(),
                    entity.getUser().getType(),
                    entity.getUser().getIsActive(),
                    entity.getUser().getCreatedAt(),
                    entity.getUser().getUpdatedAt(),
                    entity.getUser().getDeletedAt()
            );
        }
        
        return new WorkOrderHistory(
                entity.getId(),
                workOrder,
                user,
                entity.getStatusBefore(),
                entity.getStatusAfter(),
                entity.getObservation(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
