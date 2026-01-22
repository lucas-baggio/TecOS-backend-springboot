package br.com.baggiotech.tecos_api.infrastructure.persistence.filter;

import br.com.baggiotech.tecos_api.infrastructure.security.SecurityContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompanyFilterServiceImpl implements CompanyFilterService {
    
    @Override
    public UUID getCurrentCompanyId() {
        return SecurityContext.getCurrentCompanyId();
    }
}
