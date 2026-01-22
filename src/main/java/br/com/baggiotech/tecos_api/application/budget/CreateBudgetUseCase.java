package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.user.User;
import br.com.baggiotech.tecos_api.domain.user.UserRepository;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateBudgetUseCase {

    private final BudgetRepository repository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;
    private final CustomMetrics metrics;

    public CreateBudgetUseCase(BudgetRepository repository, WorkOrderRepository workOrderRepository,
                               UserRepository userRepository, CustomMetrics metrics) {
        this.repository = repository;
        this.workOrderRepository = workOrderRepository;
        this.userRepository = userRepository;
        this.metrics = metrics;
    }

    public Budget execute(UUID companyId, UUID workOrderId, BigDecimal serviceValue,
                         BigDecimal partsValue, BigDecimal totalValue, UUID createdByUserId) {
        Timer.Sample sample = metrics.startTimer();
        try {
            // Buscar work order
            WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> new EntityNotFoundException("WorkOrder", workOrderId));

            // Verificar se work_order pertence à mesma company
            if (workOrder.getCompany() == null || !workOrder.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("A ordem de serviço não pertence à sua empresa.");
            }

            // Validar se OS pode receber orçamento (não pode estar CANCELADA ou ENTREGUE)
            if (workOrder.getStatus() == OrderStatus.CANCELADO || workOrder.getStatus() == OrderStatus.ENTREGUE) {
                throw new IllegalArgumentException(
                        "Não é possível criar orçamento para uma ordem de serviço com status " + workOrder.getStatus() + ".");
            }

            // Buscar usuário criador
            User createdBy = userRepository.findById(createdByUserId)
                    .orElseThrow(() -> new EntityNotFoundException("User", createdByUserId));

            // Verificar se usuário pertence à mesma company
            if (createdBy.getCompany() == null || !createdBy.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("O usuário não pertence à empresa especificada.");
            }

            // Calcular total_value se não fornecido
            BigDecimal parts = partsValue != null ? partsValue : BigDecimal.ZERO;
            BigDecimal total = totalValue != null ? totalValue : serviceValue.add(parts);

            // Validar que total_value está correto (tolerância de 0.01)
            BigDecimal expectedTotal = serviceValue.add(parts);
            BigDecimal difference = total.subtract(expectedTotal).abs();
            if (difference.compareTo(new BigDecimal("0.01")) > 0) {
                throw new IllegalArgumentException("O valor total deve ser igual à soma do serviço e peças.");
            }

            // Criar orçamento com status PENDENTE
            Budget budget = new Budget();
            budget.setId(UUID.randomUUID());
            budget.setCompany(workOrder.getCompany());
            budget.setWorkOrder(workOrder);
            budget.setServiceValue(serviceValue.setScale(2, RoundingMode.HALF_UP));
            budget.setPartsValue(parts.setScale(2, RoundingMode.HALF_UP));
            budget.setTotalValue(total.setScale(2, RoundingMode.HALF_UP));
            budget.setStatus(BudgetStatus.PENDENTE);
            budget.setRejectionReason(null);
            budget.setCreatedBy(createdBy);
            budget.setApprovedAt(null);
            budget.setApprovalMethod(null);
            budget.setApprovedBy(null);
            budget.setCreatedAt(LocalDateTime.now());
            budget.setUpdatedAt(LocalDateTime.now());

            Budget saved = repository.save(budget);
            metrics.incrementBudgetsCreated();
            return saved;
        } finally {
            metrics.recordTimer(sample, "create");
        }
    }
}
