package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.workorderhistory.*;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.presentation.dto.workorderhistory.WorkOrderHistoryResponse;
import br.com.baggiotech.tecos_api.presentation.mapper.workorderhistory.WorkOrderHistoryMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/work-order-histories")
public class WorkOrderHistoryController {

    private final ListWorkOrderHistoriesUseCase listWorkOrderHistoriesUseCase;
    private final GetWorkOrderHistoryByIdUseCase getWorkOrderHistoryByIdUseCase;
    private final WorkOrderHistoryMapper mapper;

    public WorkOrderHistoryController(
            ListWorkOrderHistoriesUseCase listWorkOrderHistoriesUseCase,
            GetWorkOrderHistoryByIdUseCase getWorkOrderHistoryByIdUseCase,
            WorkOrderHistoryMapper mapper) {
        this.listWorkOrderHistoriesUseCase = listWorkOrderHistoriesUseCase;
        this.getWorkOrderHistoryByIdUseCase = getWorkOrderHistoryByIdUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<Page<WorkOrderHistoryResponse>> index(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) UUID workOrderId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false, defaultValue = "created_at") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 15;
        
        Page<WorkOrderHistory> historiesPage = listWorkOrderHistoriesUseCase.execute(
                workOrderId, userId, sortBy, sortOrder, pageNumber, pageSize);
        Page<WorkOrderHistoryResponse> responsePage = historiesPage.map(mapper::toResponse);
        
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderHistoryResponse> show(@PathVariable UUID id) {
        WorkOrderHistory history = getWorkOrderHistoryByIdUseCase.execute(id);
        WorkOrderHistoryResponse response = mapper.toResponse(history);
        return ResponseEntity.ok(response);
    }
}
