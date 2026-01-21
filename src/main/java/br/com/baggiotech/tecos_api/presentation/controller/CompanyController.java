package br.com.baggiotech.tecos_api.presentation.controller;

import br.com.baggiotech.tecos_api.application.company.*;
import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.presentation.dto.company.CompanyRequest;
import br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse;
import br.com.baggiotech.tecos_api.presentation.mapper.company.CompanyMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final ListCompaniesUseCase listCompaniesUseCase;
    private final CreateCompanyUseCase createCompanyUseCase;
    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeleteCompanyUseCase deleteCompanyUseCase;
    private final CompanyMapper mapper;

    public CompanyController(
            ListCompaniesUseCase listCompaniesUseCase,
            CreateCompanyUseCase createCompanyUseCase,
            GetCompanyByIdUseCase getCompanyByIdUseCase,
            UpdateCompanyUseCase updateCompanyUseCase,
            DeleteCompanyUseCase deleteCompanyUseCase,
            CompanyMapper mapper) {
        this.listCompaniesUseCase = listCompaniesUseCase;
        this.createCompanyUseCase = createCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.deleteCompanyUseCase = deleteCompanyUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> index(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int perPage) {
        
        Page<Company> companies = listCompaniesUseCase.execute(isActive, search, page, perPage);
        List<CompanyResponse> content = companies.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        Pageable pageable = PageRequest.of(page, perPage);
        Page<CompanyResponse> response = new PageImpl<>(content, pageable, companies.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> store(@Valid @RequestBody CompanyRequest request) {
        Company company = createCompanyUseCase.execute(
                request.name(),
                request.email(),
                request.whatsapp(),
                request.logoUrl(),
                request.isActive()
        );
        
        CompanyResponse response = mapper.toResponse(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> show(@PathVariable UUID id) {
        Company company = getCompanyByIdUseCase.execute(id);
        CompanyResponse response = mapper.toResponse(company);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyRequest request) {
        
        Company company = updateCompanyUseCase.execute(
                id,
                request.name(),
                request.email(),
                request.whatsapp(),
                request.logoUrl(),
                request.isActive()
        );
        
        CompanyResponse response = mapper.toResponse(company);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> destroy(@PathVariable UUID id) {
        deleteCompanyUseCase.execute(id);
        return ResponseEntity.ok(Map.of("message", "Empresa excluída com sucesso."));
    }
}
