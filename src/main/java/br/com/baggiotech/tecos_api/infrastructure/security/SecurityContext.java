package br.com.baggiotech.tecos_api.infrastructure.security;

import java.util.UUID;

public class SecurityContext {
    
    private static final ThreadLocal<UUID> currentCompanyId = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();
    
    public static void setCurrentCompanyId(UUID companyId) {
        currentCompanyId.set(companyId);
    }
    
    public static UUID getCurrentCompanyId() {
        return currentCompanyId.get();
    }
    
    public static void setCurrentUserId(UUID userId) {
        currentUserId.set(userId);
    }
    
    public static UUID getCurrentUserId() {
        return currentUserId.get();
    }
    
    public static void clear() {
        currentCompanyId.remove();
        currentUserId.remove();
    }
}
