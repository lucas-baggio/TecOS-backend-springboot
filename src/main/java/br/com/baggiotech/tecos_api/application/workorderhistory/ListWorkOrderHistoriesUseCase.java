package br.com.baggiotech.tecos_api.application.workorderhistory;

import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
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
public class ListWorkOrderHistoriesUseCase {

    private final WorkOrderHistoryRepository repository;

    public ListWorkOrderHistoriesUseCase(WorkOrderHistoryRepository repository) {
        this.repository = repository;
    }

    public Page<WorkOrderHistory> execute(UUID workOrderId, UUID userId, String sortBy,
                                         String sortOrder, int page, int size) {
        List<WorkOrderHistory> histories;

        // Aplicar filtros
        if (workOrderId != null && userId != null) {
            // Filtrar por ambos
            List<WorkOrderHistory> byWorkOrder = repository.findByWorkOrderId(workOrderId);
            List<WorkOrderHistory> byUser = repository.findByUserId(userId);
            histories = new ArrayList<>(byWorkOrder);
            histories.retainAll(byUser);
        } else if (workOrderId != null) {
            histories = new ArrayList<>(repository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId));
        } else if (userId != null) {
            histories = new ArrayList<>(repository.findByUserId(userId));
        } else {
            histories = new ArrayList<>(repository.findAll());
        }

        // Ordenação
        String sortField = sortBy != null ? sortBy : "created_at";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        histories.sort((h1, h2) -> {
            int result = 0;
            switch (sortField.toLowerCase()) {
                case "created_at":
                case "createdat":
                    result = (h1.getCreatedAt() != null ? h1.getCreatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(h2.getCreatedAt() != null ? h2.getCreatedAt() : java.time.LocalDateTime.MIN);
                    break;
                case "updated_at":
                case "updatedat":
                    result = (h1.getUpdatedAt() != null ? h1.getUpdatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(h2.getUpdatedAt() != null ? h2.getUpdatedAt() : java.time.LocalDateTime.MIN);
                    break;
                case "status_after":
                case "statusafter":
                    result = (h1.getStatusAfter() != null ? h1.getStatusAfter().name() : "")
                            .compareToIgnoreCase(h2.getStatusAfter() != null ? h2.getStatusAfter().name() : "");
                    break;
                default:
                    result = (h1.getCreatedAt() != null ? h1.getCreatedAt() : java.time.LocalDateTime.MIN)
                            .compareTo(h2.getCreatedAt() != null ? h2.getCreatedAt() : java.time.LocalDateTime.MIN);
            }
            return direction == Sort.Direction.ASC ? result : -result;
        });

        int start = page * size;
        int end = Math.min(start + size, histories.size());
        List<WorkOrderHistory> pagedHistories = start < histories.size()
                ? histories.subList(start, end)
                : List.of();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return new PageImpl<>(pagedHistories, pageable, histories.size());
    }
}
