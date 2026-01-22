package br.com.baggiotech.tecos_api.infrastructure.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final SecurityContextInterceptor securityContextInterceptor;
    
    public WebConfig(SecurityContextInterceptor securityContextInterceptor) {
        this.securityContextInterceptor = securityContextInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityContextInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/public/**",
                        "/api/auth/login",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password"
                );
    }
}
