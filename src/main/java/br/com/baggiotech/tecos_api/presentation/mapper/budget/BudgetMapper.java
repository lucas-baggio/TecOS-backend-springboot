package br.com.baggiotech.tecos_api.presentation.mapper.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.presentation.dto.budget.BudgetResponse;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {
    
    public BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCompany() != null ? budget.getCompany().getId() : null,
                budget.getCompany() != null ? budget.getCompany().getName() : null,
                budget.getWorkOrder() != null ? budget.getWorkOrder().getId() : null,
                budget.getStatus(),
                budget.getServiceValue(),
                budget.getPartsValue(),
                budget.getTotalValue(),
                budget.getRejectionReason(),
                budget.getCreatedBy() != null ? budget.getCreatedBy().getId() : null,
                budget.getCreatedBy() != null ? budget.getCreatedBy().getName() : null,
                budget.getApprovedAt(),
                budget.getApprovalMethod(),
                budget.getApprovedBy() != null ? budget.getApprovedBy().getId() : null,
                budget.getApprovedBy() != null ? budget.getApprovedBy().getName() : null,
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
