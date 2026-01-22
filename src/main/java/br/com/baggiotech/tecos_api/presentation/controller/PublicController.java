package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.publiclink.*;
import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.client.Client;
import br.com.baggiotech.tecos_api.domain.equipment.Equipment;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.presentation.dto.publiclink.PublicWorkOrderResponse;
import br.com.baggiotech.tecos_api.presentation.dto.publiclink.RejectBudgetByTokenRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final GetPublicWorkOrderByTokenUseCase getPublicWorkOrderByTokenUseCase;
    private final ApproveBudgetByTokenUseCase approveBudgetByTokenUseCase;
    private final RejectBudgetByTokenUseCase rejectBudgetByTokenUseCase;

    public PublicController(
            GetPublicWorkOrderByTokenUseCase getPublicWorkOrderByTokenUseCase,
            ApproveBudgetByTokenUseCase approveBudgetByTokenUseCase,
            RejectBudgetByTokenUseCase rejectBudgetByTokenUseCase) {
        this.getPublicWorkOrderByTokenUseCase = getPublicWorkOrderByTokenUseCase;
        this.approveBudgetByTokenUseCase = approveBudgetByTokenUseCase;
        this.rejectBudgetByTokenUseCase = rejectBudgetByTokenUseCase;
    }

    /**
     * Public route - Get work order info by token.
     * RB-06: Leitura pública sem autenticação, apenas leitura
     */
    @GetMapping("/{token}")
    public ResponseEntity<PublicWorkOrderResponse> showByToken(@PathVariable String token) {
        GetPublicWorkOrderByTokenUseCase.PublicWorkOrderData data = 
            getPublicWorkOrderByTokenUseCase.execute(token);
        
        WorkOrder workOrder = data.getWorkOrder();
        List<Budget> budgets = data.getBudgets();

        // Retornar apenas dados necessários para o cliente (sem dados sensíveis - RB-06)
        PublicWorkOrderResponse response = new PublicWorkOrderResponse(
                new PublicWorkOrderResponse.PublicWorkOrderInfo(
                        workOrder.getId(),
                        workOrder.getStatus(),
                        workOrder.getReportedDefect(),
                        workOrder.getReturnOrder(),
                        workOrder.getDeliveredAt(),
                        workOrder.getCreatedAt(),
                        workOrder.getUpdatedAt()
                ),
                workOrder.getClient() != null ? new PublicWorkOrderResponse.PublicClientInfo(
                        workOrder.getClient().getName(),
                        workOrder.getClient().getPhone(),
                        workOrder.getClient().getEmail()
                ) : null,
                workOrder.getEquipment() != null ? new PublicWorkOrderResponse.PublicEquipmentInfo(
                        workOrder.getEquipment().getType(),
                        workOrder.getEquipment().getBrand(),
                        workOrder.getEquipment().getModel(),
                        workOrder.getEquipment().getSerialNumber()
                ) : null,
                budgets.stream().map(budget -> new PublicWorkOrderResponse.PublicBudgetInfo(
                        budget.getId(),
                        budget.getServiceValue(),
                        budget.getPartsValue(),
                        budget.getTotalValue(),
                        budget.getStatus(),
                        budget.getRejectionReason(),
                        budget.getCreatedAt(),
                        budget.getApprovedAt()
                )).collect(Collectors.toList())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Public route - Approve budget by token.
     * RB-06: Aprovação pública sem autenticação
     */
    @PostMapping("/{token}/budgets/{budgetId}/approve")
    public ResponseEntity<Map<String, Object>> approveBudget(
            @PathVariable String token,
            @PathVariable UUID budgetId,
            @RequestParam(required = false) UUID userId) {
        
        Budget budget = approveBudgetByTokenUseCase.execute(token, budgetId, userId);
        
        Map<String, Object> budgetMap = new HashMap<>();
        budgetMap.put("id", budget.getId());
        budgetMap.put("totalValue", budget.getTotalValue());
        budgetMap.put("status", budget.getStatus());
        budgetMap.put("approvedAt", budget.getApprovedAt());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Orçamento aprovado com sucesso!");
        response.put("budget", budgetMap);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Public route - Reject budget by token.
     * RB-06: Rejeição pública sem autenticação, com motivo obrigatório
     */
    @PostMapping("/{token}/budgets/{budgetId}/reject")
    public ResponseEntity<Map<String, Object>> rejectBudget(
            @PathVariable String token,
            @PathVariable UUID budgetId,
            @Valid @RequestBody RejectBudgetByTokenRequest request) {
        
        Budget budget = rejectBudgetByTokenUseCase.execute(token, budgetId, request.rejectionReason());
        
        Map<String, Object> budgetMap = new HashMap<>();
        budgetMap.put("id", budget.getId());
        budgetMap.put("totalValue", budget.getTotalValue());
        budgetMap.put("status", budget.getStatus());
        budgetMap.put("rejectionReason", budget.getRejectionReason());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Orçamento rejeitado com sucesso!");
        response.put("budget", budgetMap);
        
        return ResponseEntity.ok(response);
    }
}
