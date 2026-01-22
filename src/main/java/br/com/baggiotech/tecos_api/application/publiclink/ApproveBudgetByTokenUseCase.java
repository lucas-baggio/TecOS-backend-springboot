package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApproveBudgetByTokenUseCase {

    private final PublicLinkRepository publicLinkRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public ApproveBudgetByTokenUseCase(PublicLinkRepository publicLinkRepository,
                                      BudgetRepository budgetRepository,
                                      UserRepository userRepository) {
        this.publicLinkRepository = publicLinkRepository;
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Budget execute(String token, UUID budgetId, UUID authenticatedUserId) {
        // Buscar link público
        PublicLink publicLink = publicLinkRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("PublicLink", "Token não encontrado"));

        // Buscar orçamento
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Budget", budgetId));

        // Validar que o orçamento pertence à mesma work order do link
        if (budget.getWorkOrder() == null ||
            !budget.getWorkOrder().getId().equals(publicLink.getWorkOrder().getId())) {
            throw new IllegalArgumentException("O orçamento não pertence à ordem de serviço do link.");
        }

        // Validar que o orçamento está pendente
        if (budget.getStatus() != BudgetStatus.PENDENTE) {
            throw new IllegalArgumentException("Este orçamento já foi processado.");
        }

        // Validar que a OS está em AGUARDANDO_APROVACAO
        if (publicLink.getWorkOrder().getStatus() != OrderStatus.AGUARDANDO_APROVACAO) {
            throw new IllegalArgumentException("A ordem de serviço não está aguardando aprovação.");
        }

        // RB-05: Desaprovar outros orçamentos da mesma OS
        List<Budget> otherApprovedBudgets = budgetRepository.findByWorkOrderIdAndStatus(
                budget.getWorkOrder().getId(), BudgetStatus.APROVADO);
        
        for (Budget otherBudget : otherApprovedBudgets) {
            if (!otherBudget.getId().equals(budgetId)) {
                otherBudget.setStatus(BudgetStatus.PENDENTE);
                otherBudget.setApprovedAt(null);
                otherBudget.setApprovalMethod(null);
                otherBudget.setApprovedBy(null);
                otherBudget.setUpdatedAt(LocalDateTime.now());
                budgetRepository.save(otherBudget);
            }
        }

        // Aprovar via link público
        // Se houver usuário autenticado, salvar ID para auditoria (operador aprovando sem conversar com cliente)
        User approvedBy = null;
        if (authenticatedUserId != null) {
            approvedBy = userRepository.findById(authenticatedUserId).orElse(null);
        }

        budget.setStatus(BudgetStatus.APROVADO);
        budget.setApprovedAt(LocalDateTime.now());
        budget.setApprovalMethod("link");
        budget.setApprovedBy(approvedBy);
        budget.setUpdatedAt(LocalDateTime.now());

        return budgetRepository.save(budget);
    }
}
