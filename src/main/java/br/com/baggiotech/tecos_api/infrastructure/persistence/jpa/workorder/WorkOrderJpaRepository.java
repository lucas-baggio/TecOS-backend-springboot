package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorder;

import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkOrderJpaRepository extends JpaRepository<WorkOrderJpaEntity, UUID> {
    List<WorkOrderJpaEntity> findByCompanyId(UUID companyId);
    List<WorkOrderJpaEntity> findByClientId(UUID clientId);
    List<WorkOrderJpaEntity> findByEquipmentId(UUID equipmentId);
    List<WorkOrderJpaEntity> findByTechnicianId(UUID technicianId);
    List<WorkOrderJpaEntity> findByStatus(OrderStatus status);
    
    @Query("SELECT w FROM WorkOrderJpaEntity w WHERE w.company.id = :companyId AND w.status = :status")
    List<WorkOrderJpaEntity> findByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") OrderStatus status);
    
    @Query("SELECT w FROM WorkOrderJpaEntity w WHERE w.client.id = :clientId AND w.status = :status")
    List<WorkOrderJpaEntity> findByClientIdAndStatus(@Param("clientId") UUID clientId, @Param("status") OrderStatus status);
    
    List<WorkOrderJpaEntity> findByReturnOrder(Boolean returnOrder);
    
    @Query("SELECT w FROM WorkOrderJpaEntity w WHERE " +
           "LOWER(w.reportedDefect) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.internalObservations) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.client.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<WorkOrderJpaEntity> searchByReportedDefectOrInternalObservationsOrClientName(@Param("search") String search);
}
