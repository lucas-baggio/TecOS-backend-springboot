package br.com.baggiotech.tecos_api.domain.budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {
    Budget save(Budget budget);
    Optional<Budget> findById(UUID id);
    boolean existsById(UUID id);
    List<Budget> findAll();
    List<Budget> findByCompanyId(UUID companyId);
    List<Budget> findByWorkOrderId(UUID workOrderId);
    List<Budget> findByStatus(BudgetStatus status);
    List<Budget> findByWorkOrderIdAndStatus(UUID workOrderId, BudgetStatus status);
    List<Budget> findByCompanyIdAndWorkOrderId(UUID companyId, UUID workOrderId);
    List<Budget> findByCompanyIdAndStatus(UUID companyId, BudgetStatus status);
}
