package br.com.baggiotech.tecos_api.application.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.domain.company.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListCompaniesUseCase {

    private final CompanyRepository repository;

    public ListCompaniesUseCase(CompanyRepository repository) {
        this.repository = repository;
    }

    public Page<Company> execute(Boolean isActive, String search, int page, int size) {
        List<Company> companies;

        if (isActive != null && search != null && !search.isBlank()) {
            List<Company> filteredByActive = repository.findByIsActive(isActive);
            List<Company> searched = repository.searchByNameOrEmail(search);
            var searchedIds = searched.stream()
                    .map(Company::getId)
                    .collect(Collectors.toSet());
            companies = filteredByActive.stream()
                    .filter(c -> searchedIds.contains(c.getId()))
                    .collect(Collectors.toList());
        } else if (isActive != null) {
            companies = repository.findByIsActive(isActive);
        } else if (search != null && !search.isBlank()) {
            companies = repository.searchByNameOrEmail(search);
        } else {
            companies = repository.findAll();
        }

        companies = companies.stream()
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, companies.size());
        List<Company> pagedCompanies = start < companies.size() 
                ? companies.subList(start, end) 
                : List.of();

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return new PageImpl<>(pagedCompanies, pageable, companies.size());
    }
}
