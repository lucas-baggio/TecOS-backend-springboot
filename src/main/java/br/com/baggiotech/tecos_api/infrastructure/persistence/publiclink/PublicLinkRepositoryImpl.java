package br.com.baggiotech.tecos_api.infrastructure.persistence.publiclink;

import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.publiclink.PublicLinkJpaEntity;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.publiclink.PublicLinkJpaRepository;
import br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder.WorkOrderJpaEntity;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PublicLinkRepositoryImpl implements PublicLinkRepository {

    private final PublicLinkJpaRepository jpaRepository;
    private final WorkOrderRepository workOrderRepository;

    public PublicLinkRepositoryImpl(PublicLinkJpaRepository jpaRepository,
                                   WorkOrderRepository workOrderRepository) {
        this.jpaRepository = jpaRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Override
    public PublicLink save(PublicLink publicLink) {
        PublicLinkJpaEntity entity = toJpaEntity(publicLink);
        if (publicLink.getId() != null && !jpaRepository.existsById(publicLink.getId())) {
            entity.setId(null);
        }
        PublicLinkJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<PublicLink> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void delete(PublicLink publicLink) {
        PublicLinkJpaEntity entity = toJpaEntity(publicLink);
        jpaRepository.delete(entity);
    }

    @Override
    public List<PublicLink> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PublicLink> findByWorkOrderId(UUID workOrderId) {
        return jpaRepository.findByWorkOrderId(workOrderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PublicLink> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByToken(String token) {
        return jpaRepository.existsByToken(token);
    }

    private PublicLinkJpaEntity toJpaEntity(PublicLink publicLink) {
        PublicLinkJpaEntity entity = new PublicLinkJpaEntity();
        entity.setId(publicLink.getId());
        
        if (publicLink.getWorkOrder() != null) {
            WorkOrderJpaEntity workOrderEntity = new WorkOrderJpaEntity();
            workOrderEntity.setId(publicLink.getWorkOrder().getId());
            entity.setWorkOrder(workOrderEntity);
        }
        
        entity.setToken(publicLink.getToken());
        entity.setCreatedAt(publicLink.getCreatedAt());
        entity.setUpdatedAt(publicLink.getUpdatedAt());
        return entity;
    }

    private PublicLink toDomain(PublicLinkJpaEntity entity) {
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
        
        return new PublicLink(
                entity.getId(),
                workOrder,
                entity.getToken(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
