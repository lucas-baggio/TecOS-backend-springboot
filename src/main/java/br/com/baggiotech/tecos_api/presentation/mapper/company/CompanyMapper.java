package br.com.baggiotech.tecos_api.presentation.mapper.company;

import br.com.baggiotech.tecos_api.domain.company.Company;
import br.com.baggiotech.tecos_api.presentation.dto.company.CompanyRequest;
import br.com.baggiotech.tecos_api.presentation.dto.company.CompanyResponse;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public Company toDomain(CompanyRequest request) {
        Company company = new Company();
        company.setName(request.name());
        company.setEmail(request.email());
        company.setWhatsapp(request.whatsapp());
        company.setLogoUrl(request.logoUrl());
        company.setIsActive(request.isActive() != null ? request.isActive() : true);
        return company;
    }

    public CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getEmail(),
                company.getWhatsapp(),
                company.getLogoUrl(),
                company.getIsActive(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
