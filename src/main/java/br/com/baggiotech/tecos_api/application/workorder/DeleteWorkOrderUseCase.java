package br.com.baggiotech.tecos_api.application.workorder;

import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrder;
import br.com.baggiotech.tecos_api.domain.workorder.WorkOrderRepository;
import br.com.baggiotech.tecos_api.infrastructure.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteWorkOrderUseCase {

    private final WorkOrderRepository repository;
    private final CustomMetrics metrics;

    public DeleteWorkOrderUseCase(WorkOrderRepository repository, CustomMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    public void execute(UUID id) {
        Timer.Sample sample = metrics.startTimer();
        try {
            WorkOrder workOrder = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("WorkOrder", id));

            repository.delete(workOrder);
            metrics.incrementWorkOrdersDeleted();
        } finally {
            metrics.recordTimer(sample, "delete");
        }
    }
}
