package com.github.rjhoppe.root_v2.config;

import com.github.rjhoppe.root_v2.interceptors.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;

/**
 * Spring MVC Configuration to register custom interceptors.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply the rate limit interceptor to your game API endpoints
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/game/start", "/api/game/submit");
                // You can add more patterns or use Ant-style paths like "/api/game/**"
    }
}
