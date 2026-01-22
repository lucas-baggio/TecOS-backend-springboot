package br.com.baggiotech.tecos_api.infrastructure.persistence.jpa.budget;

import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetJpaRepository extends JpaRepository<BudgetJpaEntity, UUID> {
    
    @Query("SELECT b FROM BudgetJpaEntity b WHERE b.company.id = :companyId")
    List<BudgetJpaEntity> findByCompanyId(@Param("companyId") UUID companyId);
    
    @Query("SELECT b FROM BudgetJpaEntity b WHERE b.workOrder.id = :workOrderId")
    List<BudgetJpaEntity> findByWorkOrderId(@Param("workOrderId") UUID workOrderId);
    
    List<BudgetJpaEntity> findByStatus(BudgetStatus status);
    
    @Query("SELECT b FROM BudgetJpaEntity b WHERE b.workOrder.id = :workOrderId AND b.status = :status")
    List<BudgetJpaEntity> findByWorkOrderIdAndStatus(@Param("workOrderId") UUID workOrderId, @Param("status") BudgetStatus status);
    
    @Query("SELECT b FROM BudgetJpaEntity b WHERE b.company.id = :companyId AND b.workOrder.id = :workOrderId")
    List<BudgetJpaEntity> findByCompanyIdAndWorkOrderId(@Param("companyId") UUID companyId, @Param("workOrderId") UUID workOrderId);
    
    @Query("SELECT b FROM BudgetJpaEntity b WHERE b.company.id = :companyId AND b.status = :status")
    List<BudgetJpaEntity> findByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") BudgetStatus status);
}
