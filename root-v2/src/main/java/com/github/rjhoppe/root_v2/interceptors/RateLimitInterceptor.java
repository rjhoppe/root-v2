package com.github.rjhoppe.root_v2.interceptors;

import com.github.rjhoppe.root_v2.services.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.RequiredArgsConstructor;

/**
 * Spring MVC Interceptor to apply rate limiting to incoming requests.
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request); // Helper method to get client IP

        if (!rateLimitingService.isAllowed(clientIp)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
            response.getWriter().write("Too many requests.");
            response.getWriter().flush();
            return false; // Request is not allowed to proceed to the controller
        }

        return true; // Request is allowed to proceed
    }

    private String getClientIp(HttpServletRequest request) {
        // Look for common headers used by proxies
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(".")) {
            return request.getRemoteAddr();
        }
        // If behind a proxy, X-Forwarded-For can contain multiple IPs.
        // The first one is typically the original client IP.
        return xfHeader.split(",")[0].trim();
    }
}
