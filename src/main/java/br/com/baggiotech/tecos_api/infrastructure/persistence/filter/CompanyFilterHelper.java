package br.com.baggiotech.tecos_api.infrastructure.persistence.filter;

import jakarta.persistence.EntityManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CompanyFilterHelper {

    private final CompanyFilterService companyFilterService;

    public CompanyFilterHelper(CompanyFilterService companyFilterService) {
        this.companyFilterService = companyFilterService;
    }

    public void enableCompanyFilters(EntityManager entityManager) {
        UUID companyId = companyFilterService.getCurrentCompanyId();
        
        if (companyId != null) {
            Session session = entityManager.unwrap(Session.class);
            
            Filter companyFilter = session.enableFilter("companyFilter");
            companyFilter.setParameter("companyId", companyId);
            
            Filter workOrderFilter = session.enableFilter("companyThroughWorkOrderFilter");
            workOrderFilter.setParameter("companyId", companyId);
        }
    }
}
