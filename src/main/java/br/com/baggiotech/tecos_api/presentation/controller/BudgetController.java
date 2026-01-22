package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.budget.*;
import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetStatus;
import br.com.baggiotech.tecos_api.presentation.dto.budget.ApproveBudgetRequest;
import br.com.baggiotech.tecos_api.presentation.dto.budget.BudgetRequest;
import br.com.baggiotech.tecos_api.presentation.dto.budget.BudgetResponse;
import br.com.baggiotech.tecos_api.presentation.dto.budget.RejectBudgetRequest;
import br.com.baggiotech.tecos_api.presentation.mapper.budget.BudgetMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final ListBudgetsUseCase listBudgetsUseCase;
    private final CreateBudgetUseCase createBudgetUseCase;
    private final GetBudgetByIdUseCase getBudgetByIdUseCase;
    private final ApproveBudgetUseCase approveBudgetUseCase;
    private final RejectBudgetUseCase rejectBudgetUseCase;
    private final BudgetMapper mapper;

    public BudgetController(
            ListBudgetsUseCase listBudgetsUseCase,
            CreateBudgetUseCase createBudgetUseCase,
            GetBudgetByIdUseCase getBudgetByIdUseCase,
            ApproveBudgetUseCase approveBudgetUseCase,
            RejectBudgetUseCase rejectBudgetUseCase,
            BudgetMapper mapper) {
        this.listBudgetsUseCase = listBudgetsUseCase;
        this.createBudgetUseCase = createBudgetUseCase;
        this.getBudgetByIdUseCase = getBudgetByIdUseCase;
        this.approveBudgetUseCase = approveBudgetUseCase;
        this.rejectBudgetUseCase = rejectBudgetUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<Page<BudgetResponse>> index(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID workOrderId,
            @RequestParam(required = false) BudgetStatus status,
            @RequestParam(required = false, defaultValue = "created_at") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 15;
        
        Page<Budget> budgetsPage = listBudgetsUseCase.execute(
                companyId, workOrderId, status, sortBy, sortOrder, pageNumber, pageSize);
        Page<BudgetResponse> responsePage = budgetsPage.map(mapper::toResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> store(@Valid @RequestBody BudgetRequest request) {
        Budget budget = createBudgetUseCase.execute(
                request.companyId(),
                request.workOrderId(),
                request.serviceValue(),
                request.partsValue(),
                request.totalValue(),
                request.createdBy()
        );
        
        BudgetResponse response = mapper.toResponse(budget);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> show(@PathVariable UUID id) {
        Budget budget = getBudgetByIdUseCase.execute(id);
        BudgetResponse response = mapper.toResponse(budget);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request) {
        // RB-09: Orçamento não é editável
        throw new IllegalArgumentException("Orçamento não pode ser editado. Para ajustes, crie um novo orçamento.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> destroy(@PathVariable UUID id) {
        // RB-09: Orçamento não pode ser excluído (mantém histórico)
        throw new IllegalArgumentException("Orçamento não pode ser excluído. Para ajustes, crie um novo orçamento.");
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BudgetResponse> approve(
            @PathVariable UUID id,
            @RequestParam UUID companyId,
            @Valid @RequestBody ApproveBudgetRequest request) {
        
        Budget budget = approveBudgetUseCase.execute(
                id,
                companyId,
                request.approvalMethod(),
                request.approvedBy()
        );
        
        BudgetResponse response = mapper.toResponse(budget);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BudgetResponse> reject(
            @PathVariable UUID id,
            @RequestParam UUID companyId,
            @Valid @RequestBody RejectBudgetRequest request) {
        
        Budget budget = rejectBudgetUseCase.execute(
                id,
                companyId,
                request.rejectionReason()
        );
        
        BudgetResponse response = mapper.toResponse(budget);
        return ResponseEntity.ok(response);
    }
}
