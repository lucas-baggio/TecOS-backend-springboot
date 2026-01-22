package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApproveBudgetUseCase {

    private final BudgetRepository repository;
    private final UserRepository userRepository;
    private final CustomMetrics metrics;

    public ApproveBudgetUseCase(BudgetRepository repository, UserRepository userRepository,
                                CustomMetrics metrics) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.metrics = metrics;
    }

    @Transactional
    public Budget execute(UUID budgetId, UUID companyId, String approvalMethod, UUID approvedByUserId) {
        Budget budget = repository.findById(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Budget", budgetId));

        // Verificar se pertence à mesma company
        if (budget.getCompany() == null || !budget.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Acesso negado.");
        }

        // Verificar se orçamento está PENDENTE
        if (budget.getStatus() != BudgetStatus.PENDENTE) {
            throw new IllegalArgumentException("Apenas orçamentos pendentes podem ser aprovados.");
        }

        // Validar approval_method
        if (!"presential".equals(approvalMethod) && !"link".equals(approvalMethod)) {
            throw new IllegalArgumentException("Método de aprovação deve ser 'presential' ou 'link'.");
        }

        User approvedBy = null;
        // Se não for presencial, salvar o ID do usuário logado para auditoria
        if (!"presential".equals(approvalMethod)) {
            approvedBy = userRepository.findById(approvedByUserId)
                    .orElseThrow(() -> new EntityNotFoundException("User", approvedByUserId));
        }

        // RB-05: Desaprovar outros orçamentos da mesma OS
        List<Budget> otherApprovedBudgets = repository.findByWorkOrderIdAndStatus(
                budget.getWorkOrder().getId(), BudgetStatus.APROVADO);
        
        for (Budget otherBudget : otherApprovedBudgets) {
            if (!otherBudget.getId().equals(budgetId)) {
                otherBudget.setStatus(BudgetStatus.PENDENTE);
                otherBudget.setApprovedAt(null);
                otherBudget.setApprovalMethod(null);
                otherBudget.setApprovedBy(null);
                otherBudget.setUpdatedAt(LocalDateTime.now());
                repository.save(otherBudget);
            }
        }

        // Aprovar o orçamento
        budget.setStatus(BudgetStatus.APROVADO);
        budget.setApprovedAt(LocalDateTime.now());
        budget.setApprovalMethod(approvalMethod);
        budget.setApprovedBy(approvedBy);
        budget.setUpdatedAt(LocalDateTime.now());

        Budget saved = repository.save(budget);
        metrics.incrementBudgetsUpdated();
        return saved;
    }
}
