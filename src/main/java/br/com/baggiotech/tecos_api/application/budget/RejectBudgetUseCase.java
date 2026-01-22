package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RejectBudgetUseCase {

    private final BudgetRepository repository;
    private final CustomMetrics metrics;

    public RejectBudgetUseCase(BudgetRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public Budget execute(UUID budgetId, UUID companyId, String rejectionReason) {
        Budget budget = repository.findById(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Budget", budgetId));

        // Verificar se pertence à mesma company
        if (budget.getCompany() == null || !budget.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Acesso negado.");
        }

        // Verificar se orçamento está PENDENTE ou APROVADO
        if (budget.getStatus() != BudgetStatus.PENDENTE && budget.getStatus() != BudgetStatus.APROVADO) {
            throw new IllegalArgumentException("Apenas orçamentos pendentes ou aprovados podem ser rejeitados.");
        }

        // Validar rejection_reason (mínimo 10 caracteres)
        if (rejectionReason == null || rejectionReason.trim().length() < 10) {
            throw new IllegalArgumentException("O motivo da rejeição deve ter no mínimo 10 caracteres.");
        }

        // Rejeitar o orçamento
        budget.setStatus(BudgetStatus.REJEITADO);
        budget.setRejectionReason(rejectionReason.trim());
        budget.setUpdatedAt(LocalDateTime.now());

        Budget saved = repository.save(budget);
        metrics.incrementBudgetsUpdated();
        return saved;
    }
}
