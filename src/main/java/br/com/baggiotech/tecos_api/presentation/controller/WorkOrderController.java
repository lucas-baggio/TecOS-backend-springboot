package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.workorder.*;
import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.presentation.dto.workorder.UpdateWorkOrderStatusRequest;
import br.com.baggiotech.tecos_api.presentation.dto.workorder.WorkOrderRequest;
import br.com.baggiotech.tecos_api.presentation.dto.workorder.WorkOrderResponse;
import br.com.baggiotech.tecos_api.presentation.mapper.workorder.WorkOrderMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final ListWorkOrdersUseCase listWorkOrdersUseCase;
    private final CreateWorkOrderUseCase createWorkOrderUseCase;
    private final GetWorkOrderByIdUseCase getWorkOrderByIdUseCase;
    private final UpdateWorkOrderUseCase updateWorkOrderUseCase;
    private final UpdateWorkOrderStatusUseCase updateWorkOrderStatusUseCase;
    private final CancelWorkOrderUseCase cancelWorkOrderUseCase;
    private final WorkOrderMapper mapper;

    public WorkOrderController(
            ListWorkOrdersUseCase listWorkOrdersUseCase,
            CreateWorkOrderUseCase createWorkOrderUseCase,
            GetWorkOrderByIdUseCase getWorkOrderByIdUseCase,
            UpdateWorkOrderUseCase updateWorkOrderUseCase,
            UpdateWorkOrderStatusUseCase updateWorkOrderStatusUseCase,
            CancelWorkOrderUseCase cancelWorkOrderUseCase,
            WorkOrderMapper mapper) {
        this.listWorkOrdersUseCase = listWorkOrdersUseCase;
        this.createWorkOrderUseCase = createWorkOrderUseCase;
        this.getWorkOrderByIdUseCase = getWorkOrderByIdUseCase;
        this.updateWorkOrderUseCase = updateWorkOrderUseCase;
        this.updateWorkOrderStatusUseCase = updateWorkOrderStatusUseCase;
        this.cancelWorkOrderUseCase = cancelWorkOrderUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<Page<WorkOrderResponse>> index(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID equipmentId,
            @RequestParam(required = false) UUID technicianId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Boolean returnOrder,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "created_at") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 15;
        
        Page<WorkOrder> workOrdersPage = listWorkOrdersUseCase.execute(
                companyId, clientId, equipmentId, technicianId, status, returnOrder, search,
                sortBy, sortOrder, pageNumber, pageSize);
        Page<WorkOrderResponse> responsePage = workOrdersPage.map(mapper::toResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping
    public ResponseEntity<WorkOrderResponse> store(
            @Valid @RequestBody WorkOrderRequest request,
            @RequestParam(required = false) UUID createdBy) {
        WorkOrder workOrder = createWorkOrderUseCase.execute(
                request.companyId(),
                request.clientId(),
                request.equipmentId(),
                request.technicianId(),
                request.reportedDefect(),
                request.internalObservations(),
                request.returnOrder(),
                request.originWorkOrderId(),
                createdBy
        );
        
        WorkOrderResponse response = mapper.toResponse(workOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> show(@PathVariable UUID id) {
        WorkOrder workOrder = getWorkOrderByIdUseCase.execute(id);
        WorkOrderResponse response = mapper.toResponse(workOrder);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkOrderRequest request) {
        
        WorkOrder workOrder = updateWorkOrderUseCase.execute(
                id,
                request.companyId(),
                request.clientId(),
                request.equipmentId(),
                request.technicianId(),
                request.reportedDefect(),
                request.internalObservations(),
                request.returnOrder(),
                request.originWorkOrderId()
        );
        
        WorkOrderResponse response = mapper.toResponse(workOrder);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WorkOrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkOrderStatusRequest request,
            @RequestParam(required = false) String observation,
            @RequestParam(required = false) UUID userId) {
        
        WorkOrder workOrder = updateWorkOrderStatusUseCase.execute(id, request.status(), observation, userId);
        WorkOrderResponse response = mapper.toResponse(workOrder);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<WorkOrderResponse> cancel(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID userId) {
        WorkOrder workOrder = cancelWorkOrderUseCase.execute(id, userId);
        WorkOrderResponse response = mapper.toResponse(workOrder);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> destroy(@PathVariable UUID id) {
        // RB-03: OS nunca é apagada - apenas pode ser cancelada
        throw new IllegalArgumentException("Ordem de serviço não pode ser excluída. Use a ação de cancelar se necessário.");
    }
}
