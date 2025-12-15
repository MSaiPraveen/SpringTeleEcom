package com.example.SpringTeleEcom.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

/**
 * Intercepts all HTTP requests and logs them for debugging.
 * Useful for tracking API calls, failed requests, and performance issues.
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = Instant.now().toEpochMilli();
        request.setAttribute("startTime", startTime);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String authHeader = request.getHeader("Authorization");

        log.info("📥 Incoming Request: {} {} {} | IP: {} | Auth: {}",
                method,
                uri,
                queryString != null ? "?" + queryString : "",
                clientIp,
                authHeader != null ? "Bearer ***" : "None"
        );

        if (log.isDebugEnabled()) {
            log.debug("   User-Agent: {}", userAgent);
            log.debug("   Origin: {}", request.getHeader("Origin"));
            log.debug("   Referer: {}", request.getHeader("Referer"));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = Instant.now().toEpochMilli();
        long duration = endTime - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // Color-code based on status
        String emoji = getStatusEmoji(status);
        String level = status >= 500 ? "ERROR" : status >= 400 ? "WARN" : "INFO";

        String logMessage = String.format("%s Response: %s %s | Status: %d | Duration: %dms",
                emoji, method, uri, status, duration);

        switch (level) {
            case "ERROR" -> log.error(logMessage);
            case "WARN" -> log.warn(logMessage);
            default -> log.info(logMessage);
        }

        // Log exception if present
        if (ex != null) {
            log.error("💥 Exception during request processing: {}", ex.getMessage(), ex);
        }

        // Warn on slow requests
        if (duration > 2000) {
            log.warn("⏱️ SLOW REQUEST: {} {} took {}ms", method, uri, duration);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) return "✅";
        if (status >= 300 && status < 400) return "↪️";
        if (status >= 400 && status < 500) return "⚠️";
        return "❌";
    }
}

