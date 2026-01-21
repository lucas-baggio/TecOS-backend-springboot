package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import br.com.baggiotech.tecos_api.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCompanyByIdUseCase {

    private final CompanyRepository repository;

    public GetCompanyByIdUseCase(CompanyRepository repository) {
        this.repository = repository;
    }

    public Company execute(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company", id));
    }
}
