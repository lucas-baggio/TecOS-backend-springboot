package br.com.baggiotech.tecos_api.application.budget;

import br.com.baggiotech.tecos_api.domain.budget.Budget;
import br.com.baggiotech.tecos_api.domain.budget.BudgetRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetBudgetByIdUseCase {

    private final BudgetRepository repository;

    public GetBudgetByIdUseCase(BudgetRepository repository) {
        this.repository = repository;
    }

    public Budget execute(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Budget", id));
    }
}
