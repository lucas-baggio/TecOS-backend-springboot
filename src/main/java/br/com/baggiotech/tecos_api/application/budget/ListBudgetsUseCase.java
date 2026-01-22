package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ListBudgetsUseCase {

    private final BudgetRepository repository;

    public ListBudgetsUseCase(BudgetRepository repository) {
        this.repository = repository;
    }

    public Page<Budget> execute(UUID companyId, UUID workOrderId, BudgetStatus status,
                               String sortBy, String sortOrder, int page, int size) {
        List<Budget> budgets;

        // Aplicar filtros
        if (workOrderId != null && status != null) {
            budgets = new ArrayList<>(repository.findByWorkOrderIdAndStatus(workOrderId, status));
        } else if (companyId != null && status != null) {
            budgets = new ArrayList<>(repository.findByCompanyIdAndStatus(companyId, status));
        } else if (companyId != null && workOrderId != null) {
            budgets = new ArrayList<>(repository.findByCompanyIdAndWorkOrderId(companyId, workOrderId));
        } else if (workOrderId != null) {
            budgets = new ArrayList<>(repository.findByWorkOrderId(workOrderId));
        } else if (companyId != null) {
            budgets = new ArrayList<>(repository.findByCompanyId(companyId));
        } else if (status != null) {
            budgets = new ArrayList<>(repository.findByStatus(status));
        } else {
            budgets = new ArrayList<>(repository.findAll());
        }

        // Ordenação
        String sortField = sortBy != null ? sortBy : "created_at";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        budgets.sort((b1, b2) -> {
            int result = 0;
            switch (sortField.toLowerCase()) {
                case "status":
                    result = (b1.getStatus() != null ? b1.getStatus().name() : "")
                            .compareToIgnoreCase(b2.getStatus() != null ? b2.getStatus().name() : "");
                    break;
                case "total_value":
                case "totalvalue":
                    result = (b1.getTotalValue() != null ? b1.getTotalValue() : java.math.BigDecimal.ZERO)
                            .compareTo(b2.getTotalValue() != null ? b2.getTotalValue() : java.math.BigDecimal.ZERO);
                    break;
                case "created_at":
                case "createdat":
                    result = (b1.getCreatedAt() != null ? b1.getCreatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(b2.getCreatedAt() != null ? b2.getCreatedAt() : java.time.LocalDateTime.MIN);
                    break;
                case "updated_at":
                case "updatedat":
                    result = (b1.getUpdatedAt() != null ? b1.getUpdatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(b2.getUpdatedAt() != null ? b2.getUpdatedAt() : java.time.LocalDateTime.MIN);
                    break;
                case "approved_at":
                case "approvedat":
                    result = (b1.getApprovedAt() != null ? b1.getApprovedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(b2.getApprovedAt() != null ? b2.getApprovedAt() : java.time.LocalDateTime.MIN);
                    break;
                default:
                    result = (b1.getCreatedAt() != null ? b1.getCreatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(b2.getCreatedAt() != null ? b2.getCreatedAt() : java.time.LocalDateTime.MIN);
            }
            return direction == Sort.Direction.ASC ? result : -result;
        });

        int start = page * size;
        int end = Math.min(start + size, budgets.size());
        List<Budget> pagedBudgets = start < budgets.size()
                ? budgets.subList(start, end)
                : List.of();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return new PageImpl<>(pagedBudgets, pageable, budgets.size());
    }
}
