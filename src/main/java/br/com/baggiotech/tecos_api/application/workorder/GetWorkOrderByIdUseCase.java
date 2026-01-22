package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetWorkOrderByIdUseCase {

    private final WorkOrderRepository repository;

    public GetWorkOrderByIdUseCase(WorkOrderRepository repository) {
        this.repository = repository;
    }

    public WorkOrder execute(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder", id));
    }
}
