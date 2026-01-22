package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.workorder.OrderStatus;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ListWorkOrdersUseCase {

    private final WorkOrderRepository repository;

    public ListWorkOrdersUseCase(WorkOrderRepository repository) {
        this.repository = repository;
    }

    public Page<WorkOrder> execute(UUID companyId, UUID clientId, UUID equipmentId, UUID technicianId,
                                   OrderStatus status, Boolean returnOrder, String search,
                                   String sortBy, String sortOrder, int page, int size) {
        List<WorkOrder> workOrders;

        // Aplicar filtros básicos primeiro
        if (companyId != null && status != null) {
            workOrders = new ArrayList<>(repository.findByCompanyIdAndStatus(companyId, status));
        } else if (clientId != null && status != null) {
            workOrders = new ArrayList<>(repository.findByClientIdAndStatus(clientId, status));
        } else if (companyId != null) {
            workOrders = new ArrayList<>(repository.findByCompanyId(companyId));
        } else if (clientId != null) {
            workOrders = new ArrayList<>(repository.findByClientId(clientId));
        } else if (equipmentId != null) {
            workOrders = new ArrayList<>(repository.findByEquipmentId(equipmentId));
        } else if (technicianId != null) {
            workOrders = new ArrayList<>(repository.findByTechnicianId(technicianId));
        } else if (status != null) {
            workOrders = new ArrayList<>(repository.findByStatus(status));
        } else {
            workOrders = new ArrayList<>(repository.findAll());
        }

        // Aplicar filtro de returnOrder
        if (returnOrder != null) {
            List<WorkOrder> filteredByReturn = repository.findByReturnOrder(returnOrder);
            var returnIds = filteredByReturn.stream()
                    .map(WorkOrder::getId)
                    .collect(java.util.stream.Collectors.toSet());
            workOrders = new ArrayList<>(workOrders.stream()
                    .filter(wo -> returnIds.contains(wo.getId()))
                    .collect(java.util.stream.Collectors.toList()));
        }

        // Aplicar busca
        if (search != null && !search.isBlank()) {
            List<WorkOrder> searched = repository.searchByReportedDefectOrInternalObservationsOrClientName(search);
            var searchedIds = searched.stream()
                    .map(WorkOrder::getId)
                    .collect(java.util.stream.Collectors.toSet());
            workOrders = new ArrayList<>(workOrders.stream()
                    .filter(wo -> searchedIds.contains(wo.getId()))
                    .collect(java.util.stream.Collectors.toList()));
        }

        String sortField = sortBy != null ? sortBy : "created_at";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        workOrders.sort((w1, w2) -> {
            int result = 0;
            switch (sortField.toLowerCase()) {
                case "status":
                    result = (w1.getStatus() != null ? w1.getStatus().name() : "").compareToIgnoreCase(w2.getStatus() != null ? w2.getStatus().name() : "");
                    break;
                case "created_at":
                case "createdat":
                    result = (w1.getCreatedAt() != null ? w1.getCreatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(w2.getCreatedAt() != null ? w2.getCreatedAt() : java.time.LocalDateTime.MIN);
                    break;
                case "updated_at":
                case "updatedat":
                    result = (w1.getUpdatedAt() != null ? w1.getUpdatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(w2.getUpdatedAt() != null ? w2.getUpdatedAt() : java.time.LocalDateTime.MIN);
                    break;
                default:
                    result = (w1.getCreatedAt() != null ? w1.getCreatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(w2.getCreatedAt() != null ? w2.getCreatedAt() : java.time.LocalDateTime.MIN);
            }
            return direction == Sort.Direction.ASC ? result : -result;
        });

        int start = page * size;
        int end = Math.min(start + size, workOrders.size());
        List<WorkOrder> pagedWorkOrders = start < workOrders.size() 
                ? workOrders.subList(start, end) 
                : List.of();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return new PageImpl<>(pagedWorkOrders, pageable, workOrders.size());
    }
}
