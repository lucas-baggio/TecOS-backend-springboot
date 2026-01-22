package br.com.baggiotech.tecos_api.infrastructure.persistence;

import br.com.baggiotech.tecos_api.infrastructure.persistence.filter.CompanyFilterHelper;
import jakarta.persistence.EntityManager;

public abstract class BaseRepository {
    
    protected final EntityManager entityManager;
    protected final CompanyFilterHelper companyFilterHelper;
    
    public BaseRepository(EntityManager entityManager, CompanyFilterHelper companyFilterHelper) {
        this.entityManager = entityManager;
        this.companyFilterHelper = companyFilterHelper;
    }
    
    protected void enableFilters() {
        companyFilterHelper.enableCompanyFilters(entityManager);
    }
}
