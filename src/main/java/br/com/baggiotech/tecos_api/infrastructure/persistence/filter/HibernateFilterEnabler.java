package br.com.baggiotech.tecos_api.infrastructure.persistence.filter;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.PersistenceUnit;
import java.util.UUID;

@Component
public class HibernateFilterEnabler implements PreLoadEventListener {
    
    private final CompanyFilterService companyFilterService;
    
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    
    public HibernateFilterEnabler(CompanyFilterService companyFilterService) {
        this.companyFilterService = companyFilterService;
    }
    
    @PostConstruct
    public void register() {
        if (entityManagerFactory != null) {
            try {
                SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
                if (sessionFactory instanceof SessionFactoryImpl) {
                    EventListenerRegistry registry = ((SessionFactoryImpl) sessionFactory)
                            .getServiceRegistry()
                            .getService(EventListenerRegistry.class);
                    if (registry != null) {
                        registry.appendListeners(EventType.PRE_LOAD, this);
                    }
                }
            } catch (Exception e) {
            }
        }
    }
    
    @Override
    public void onPreLoad(PreLoadEvent event) {
        Session session = event.getSession();
        if (session != null) {
            enableFiltersOnSession(session);
        }
    }
    
    private void enableFiltersOnSession(Session session) {
        UUID companyId = companyFilterService.getCurrentCompanyId();
        
        if (companyId != null) {
            try {
                try {
                    Filter filter = session.enableFilter("companyFilter");
                    filter.setParameter("companyId", companyId);
                } catch (IllegalArgumentException e) {
                }
                
                try {
                    Filter filter = session.enableFilter("companyThroughWorkOrderFilter");
                    filter.setParameter("companyId", companyId);
                } catch (IllegalArgumentException e) {
                }
            } catch (Exception e) {
            }
        }
    }
}
