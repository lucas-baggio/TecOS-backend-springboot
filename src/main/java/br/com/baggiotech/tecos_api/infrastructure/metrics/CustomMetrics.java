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
