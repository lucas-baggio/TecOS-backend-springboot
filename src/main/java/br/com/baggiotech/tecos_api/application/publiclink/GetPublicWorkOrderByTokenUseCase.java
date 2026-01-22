package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetPublicWorkOrderByTokenUseCase {

    private final PublicLinkRepository publicLinkRepository;
    private final BudgetRepository budgetRepository;

    public GetPublicWorkOrderByTokenUseCase(PublicLinkRepository publicLinkRepository,
                                          BudgetRepository budgetRepository) {
        this.publicLinkRepository = publicLinkRepository;
        this.budgetRepository = budgetRepository;
    }

    public PublicWorkOrderData execute(String token) {
        // Buscar link público
        PublicLink publicLink = publicLinkRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("PublicLink", "Token não encontrado"));

        WorkOrder workOrder = publicLink.getWorkOrder();
        if (workOrder == null) {
            throw new EntityNotFoundException("WorkOrder", "WorkOrder não encontrado no link público");
        }

        // Buscar orçamentos ordenados por created_at desc
        List<Budget> budgets = budgetRepository.findByWorkOrderId(workOrder.getId());
        budgets = budgets.stream()
                .sorted(Comparator.comparing(Budget::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        return new PublicWorkOrderData(workOrder, budgets);
    }

    public static class PublicWorkOrderData {
        private final WorkOrder workOrder;
        private final List<Budget> budgets;

        public PublicWorkOrderData(WorkOrder workOrder, List<Budget> budgets) {
            this.workOrder = workOrder;
            this.budgets = budgets;
        }

        public WorkOrder getWorkOrder() {
            return workOrder;
        }

        public List<Budget> getBudgets() {
            return budgets;
        }
    }
}
