package br.com.baggiotech.tecos_api.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CustomMetrics {

    private final Counter companiesCreatedCounter;
    private final Counter companiesUpdatedCounter;
    private final Counter companiesDeletedCounter;
    private final Counter clientsCreatedCounter;
    private final Counter clientsUpdatedCounter;
    private final Counter clientsDeletedCounter;
    private final Counter equipmentsCreatedCounter;
    private final Counter equipmentsUpdatedCounter;
    private final Counter equipmentsDeletedCounter;
    private final Counter workOrdersCreatedCounter;
    private final Counter workOrdersUpdatedCounter;
    private final Counter workOrdersDeletedCounter;
    private final Counter budgetsCreatedCounter;
    private final Counter budgetsUpdatedCounter;
    private final Counter budgetsDeletedCounter;
    private final MeterRegistry meterRegistry;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.companiesCreatedCounter = Counter.builder("tecos_companies_created_total")
                .description("Total number of companies created")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.companiesUpdatedCounter = Counter.builder("tecos_companies_updated_total")
                .description("Total number of companies updated")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.companiesDeletedCounter = Counter.builder("tecos_companies_deleted_total")
                .description("Total number of companies deleted")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.clientsCreatedCounter = Counter.builder("tecos_clients_created_total")
                .description("Total number of clients created")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.clientsUpdatedCounter = Counter.builder("tecos_clients_updated_total")
                .description("Total number of clients updated")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.clientsDeletedCounter = Counter.builder("tecos_clients_deleted_total")
                .description("Total number of clients deleted")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.equipmentsCreatedCounter = Counter.builder("tecos_equipments_created_total")
                .description("Total number of equipments created")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.equipmentsUpdatedCounter = Counter.builder("tecos_equipments_updated_total")
                .description("Total number of equipments updated")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.equipmentsDeletedCounter = Counter.builder("tecos_equipments_deleted_total")
                .description("Total number of equipments deleted")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.workOrdersCreatedCounter = Counter.builder("tecos_work_orders_created_total")
                .description("Total number of work orders created")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.workOrdersUpdatedCounter = Counter.builder("tecos_work_orders_updated_total")
                .description("Total number of work orders updated")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.workOrdersDeletedCounter = Counter.builder("tecos_work_orders_deleted_total")
                .description("Total number of work orders deleted")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.budgetsCreatedCounter = Counter.builder("tecos_budgets_created_total")
                .description("Total number of budgets created")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.budgetsUpdatedCounter = Counter.builder("tecos_budgets_updated_total")
                .description("Total number of budgets updated")
                .tag("application", "tecos-api")
                .register(meterRegistry);

        this.budgetsDeletedCounter = Counter.builder("tecos_budgets_deleted_total")
                .description("Total number of budgets deleted")
                .tag("application", "tecos-api")
                .register(meterRegistry);
    }

    public void incrementCompaniesCreated() {
        companiesCreatedCounter.increment();
    }

    public void incrementCompaniesUpdated() {
        companiesUpdatedCounter.increment();
    }

    public void incrementCompaniesDeleted() {
        companiesDeletedCounter.increment();
    }

    public void incrementClientsCreated() {
        clientsCreatedCounter.increment();
    }

    public void incrementClientsUpdated() {
        clientsUpdatedCounter.increment();
    }

    public void incrementClientsDeleted() {
        clientsDeletedCounter.increment();
    }

    public void incrementEquipmentsCreated() {
        equipmentsCreatedCounter.increment();
    }

    public void incrementEquipmentsUpdated() {
        equipmentsUpdatedCounter.increment();
    }

    public void incrementEquipmentsDeleted() {
        equipmentsDeletedCounter.increment();
    }

    public void incrementWorkOrdersCreated() {
        workOrdersCreatedCounter.increment();
    }

    public void incrementWorkOrdersUpdated() {
        workOrdersUpdatedCounter.increment();
    }

    public void incrementWorkOrdersDeleted() {
        workOrdersDeletedCounter.increment();
    }

    public void incrementBudgetsCreated() {
        budgetsCreatedCounter.increment();
    }

    public void incrementBudgetsUpdated() {
        budgetsUpdatedCounter.increment();
    }

    public void incrementBudgetsDeleted() {
        budgetsDeletedCounter.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordTimer(Timer.Sample sample, String operation) {
        Timer timer = Timer.builder("tecos_companies_operation_duration_seconds")
                .description("Duration of company operations")
                .tag("application", "tecos-api")
                .tag("operation", operation)
                .register(meterRegistry);
        sample.stop(timer);
    }
}
