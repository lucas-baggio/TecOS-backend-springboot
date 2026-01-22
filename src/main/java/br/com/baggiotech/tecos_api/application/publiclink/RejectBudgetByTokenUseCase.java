package br.com.baggiotech.tecos_api.application.publiclink;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLink;
import br.com.baggiotech.tecos_api.domain.publiclink.PublicLinkRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RejectBudgetByTokenUseCase {

    private final PublicLinkRepository publicLinkRepository;
    private final BudgetRepository budgetRepository;

    public RejectBudgetByTokenUseCase(PublicLinkRepository publicLinkRepository,
                                     BudgetRepository budgetRepository) {
        this.publicLinkRepository = publicLinkRepository;
        this.budgetRepository = budgetRepository;
    }

    public Budget execute(String token, UUID budgetId, String rejectionReason) {
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

        // Validar motivo da rejeição (obrigatório - RB-05)
        if (rejectionReason == null || rejectionReason.trim().length() < 10) {
            throw new IllegalArgumentException("O motivo da rejeição deve ter no mínimo 10 caracteres.");
        }

        // Rejeitar orçamento
        budget.setStatus(BudgetStatus.REJEITADO);
        budget.setRejectionReason(rejectionReason.trim());
        budget.setUpdatedAt(LocalDateTime.now());

        return budgetRepository.save(budget);
    }
}
