package br.com.baggiotech.tecos_api.infrastructure.persistence.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.budget.BudgetJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.budget.BudgetJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.client.ClientJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.company.CompanyJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.equipment.EquipmentJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.user.UserJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BudgetRepositoryImpl implements BudgetRepository {

    private final BudgetJpaRepository jpaRepository;
    private final WorkOrderRepository workOrderRepository;

    public BudgetRepositoryImpl(BudgetJpaRepository jpaRepository, WorkOrderRepository workOrderRepository) {
        this.jpaRepository = jpaRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Override
    public Budget save(Budget budget) {
        BudgetJpaEntity entity = toJpaEntity(budget);
        if (budget.getId() != null && !jpaRepository.existsById(budget.getId())) {
            entity.setId(null);
        }
        BudgetJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Budget> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Budget> findByCompanyId(UUID companyId) {
        return jpaRepository.findByCompanyId(companyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Budget> findByWorkOrderId(UUID workOrderId) {
        return jpaRepository.findByWorkOrderId(workOrderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Budget> findByStatus(BudgetStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Budget> findByWorkOrderIdAndStatus(UUID workOrderId, BudgetStatus status) {
        return jpaRepository.findByWorkOrderIdAndStatus(workOrderId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Budget> findByCompanyIdAndWorkOrderId(UUID companyId, UUID workOrderId) {
        return jpaRepository.findByCompanyIdAndWorkOrderId(companyId, workOrderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Budget> findByCompanyIdAndStatus(UUID companyId, BudgetStatus status) {
        return jpaRepository.findByCompanyIdAndStatus(companyId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private BudgetJpaEntity toJpaEntity(Budget budget) {
        BudgetJpaEntity entity = new BudgetJpaEntity();
        entity.setId(budget.getId());
        
        if (budget.getCompany() != null) {
            CompanyJpaEntity companyEntity = new CompanyJpaEntity();
            companyEntity.setId(budget.getCompany().getId());
            entity.setCompany(companyEntity);
        }
        
        if (budget.getWorkOrder() != null) {
            WorkOrderJpaEntity workOrderEntity = new WorkOrderJpaEntity();
            workOrderEntity.setId(budget.getWorkOrder().getId());
            entity.setWorkOrder(workOrderEntity);
        }
        
        entity.setServiceValue(budget.getServiceValue());
        entity.setPartsValue(budget.getPartsValue());
        entity.setTotalValue(budget.getTotalValue());
        entity.setStatus(budget.getStatus());
        entity.setRejectionReason(budget.getRejectionReason());
        
        if (budget.getCreatedBy() != null) {
            UserJpaEntity createdByEntity = new UserJpaEntity();
            createdByEntity.setId(budget.getCreatedBy().getId());
            entity.setCreatedBy(createdByEntity);
        }
        
        entity.setApprovedAt(budget.getApprovedAt());
        entity.setApprovalMethod(budget.getApprovalMethod());
        
        if (budget.getApprovedBy() != null) {
            UserJpaEntity approvedByEntity = new UserJpaEntity();
            approvedByEntity.setId(budget.getApprovedBy().getId());
            entity.setApprovedBy(approvedByEntity);
        }
        
        entity.setCreatedAt(budget.getCreatedAt());
        entity.setUpdatedAt(budget.getUpdatedAt());
        return entity;
    }

    private Budget toDomain(BudgetJpaEntity entity) {
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
        
        User createdBy = null;
        if (entity.getCreatedBy() != null) {
            Company createdByCompany = null;
            if (entity.getCreatedBy().getCompany() != null) {
                createdByCompany = new Company(
                        entity.getCreatedBy().getCompany().getId(),
                        entity.getCreatedBy().getCompany().getName(),
                        entity.getCreatedBy().getCompany().getEmail(),
                        entity.getCreatedBy().getCompany().getWhatsapp(),
                        entity.getCreatedBy().getCompany().getLogoUrl(),
                        entity.getCreatedBy().getCompany().getIsActive(),
                        entity.getCreatedBy().getCompany().getCreatedAt(),
                        entity.getCreatedBy().getCompany().getUpdatedAt()
                );
            }
            
            createdBy = new User(
                    entity.getCreatedBy().getId(),
                    createdByCompany,
                    entity.getCreatedBy().getName(),
                    entity.getCreatedBy().getEmail(),
                    entity.getCreatedBy().getPassword(),
                    entity.getCreatedBy().getType(),
                    entity.getCreatedBy().getIsActive(),
                    entity.getCreatedBy().getCreatedAt(),
                    entity.getCreatedBy().getUpdatedAt(),
                    entity.getCreatedBy().getDeletedAt()
            );
        }
        
        User approvedBy = null;
        if (entity.getApprovedBy() != null) {
            Company approvedByCompany = null;
            if (entity.getApprovedBy().getCompany() != null) {
                approvedByCompany = new Company(
                        entity.getApprovedBy().getCompany().getId(),
                        entity.getApprovedBy().getCompany().getName(),
                        entity.getApprovedBy().getCompany().getEmail(),
                        entity.getApprovedBy().getCompany().getWhatsapp(),
                        entity.getApprovedBy().getCompany().getLogoUrl(),
                        entity.getApprovedBy().getCompany().getIsActive(),
                        entity.getApprovedBy().getCompany().getCreatedAt(),
                        entity.getApprovedBy().getCompany().getUpdatedAt()
                );
            }
            
            approvedBy = new User(
                    entity.getApprovedBy().getId(),
                    approvedByCompany,
                    entity.getApprovedBy().getName(),
                    entity.getApprovedBy().getEmail(),
                    entity.getApprovedBy().getPassword(),
                    entity.getApprovedBy().getType(),
                    entity.getApprovedBy().getIsActive(),
                    entity.getApprovedBy().getCreatedAt(),
                    entity.getApprovedBy().getUpdatedAt(),
                    entity.getApprovedBy().getDeletedAt()
            );
        }
        
        return new Budget(
                entity.getId(),
                company,
                workOrder,
                entity.getServiceValue(),
                entity.getPartsValue(),
                entity.getTotalValue(),
                entity.getStatus(),
                entity.getRejectionReason(),
                createdBy,
                entity.getApprovedAt(),
                entity.getApprovalMethod(),
                approvedBy,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
