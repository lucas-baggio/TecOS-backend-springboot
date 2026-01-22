package br.com.baggiotech.tecos_api.infrastructure.security;

import br.com.baggiotech.tecos_api.application.user.GetUserByIdUseCase;
import br.com.baggiotech.tecos_api.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class SecurityContextInterceptor implements HandlerInterceptor {
    
    private final GetUserByIdUseCase getUserByIdUseCase;
    
    public SecurityContextInterceptor(GetUserByIdUseCase getUserByIdUseCase) {
        this.getUserByIdUseCase = getUserByIdUseCase;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdHeader = request.getHeader("X-User-Id");
        
        if (userIdHeader != null) {
            try {
                UUID userId = UUID.fromString(userIdHeader);
                User user = getUserByIdUseCase.execute(userId);
                
                if (user.getCompany() != null) {
                    SecurityContext.setCurrentCompanyId(user.getCompany().getId());
                    SecurityContext.setCurrentUserId(user.getId());
                }
            } catch (Exception e) {
                SecurityContext.clear();
            }
        } else {
            SecurityContext.clear();
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        SecurityContext.clear();
    }
}
