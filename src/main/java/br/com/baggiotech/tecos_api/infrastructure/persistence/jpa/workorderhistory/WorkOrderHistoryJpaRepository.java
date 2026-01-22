package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.workorderhistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkOrderHistoryJpaRepository extends JpaRepository<WorkOrderHistoryJpaEntity, UUID> {
    
    @Query("SELECT h FROM WorkOrderHistoryJpaEntity h WHERE h.workOrder.id = :workOrderId")
    List<WorkOrderHistoryJpaEntity> findByWorkOrderId(@Param("workOrderId") UUID workOrderId);
    
    @Query("SELECT h FROM WorkOrderHistoryJpaEntity h WHERE h.workOrder.id = :workOrderId ORDER BY h.createdAt DESC")
    List<WorkOrderHistoryJpaEntity> findByWorkOrderIdOrderByCreatedAtDesc(@Param("workOrderId") UUID workOrderId);
    
    @Query("SELECT h FROM WorkOrderHistoryJpaEntity h WHERE h.user.id = :userId")
    List<WorkOrderHistoryJpaEntity> findByUserId(@Param("userId") UUID userId);
}
