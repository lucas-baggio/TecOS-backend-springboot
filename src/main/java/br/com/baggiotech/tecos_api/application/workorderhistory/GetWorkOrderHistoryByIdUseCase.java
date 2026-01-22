package br.com.baggiotech.tecos_api.application.workorderhistory;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistory;
import br.com.baggiotech.tecos_api.domain.workorderhistory.WorkOrderHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetWorkOrderHistoryByIdUseCase {

    private final WorkOrderHistoryRepository repository;

    public GetWorkOrderHistoryByIdUseCase(WorkOrderHistoryRepository repository) {
        this.repository = repository;
    }

    public WorkOrderHistory execute(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrderHistory", id));
    }
}
