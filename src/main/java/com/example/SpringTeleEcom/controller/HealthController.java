package com.example.SpringTeleEcom.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health check endpoint with detailed diagnostics.
 * Useful for Render health checks and debugging deployment issues.
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationContext applicationContext;

    /**
     * Basic health check - returns 200 if app is running
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Health check requested");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        health.put("service", "SpringTeleEcom");
        health.put("version", "6.0.1");

        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check with database connectivity
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        log.info("🏥 Detailed health check initiated");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("service", "SpringTeleEcom");
        response.put("version", "6.0.1");

        // Check database connectivity
        Map<String, Object> database = new HashMap<>();
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            database.put("status", result != null && result == 1 ? "UP" : "DOWN");
            database.put("connection", "active");

            // Get database metadata
            String dbName = jdbcTemplate.queryForObject(
                    "SELECT current_database()", String.class);
            database.put("database", dbName);

            log.info("✅ Database health: UP (database: {})", dbName);

        } catch (Exception e) {
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
            log.error("❌ Database health check failed", e);
        }
        response.put("database", database);

        // Check application state
        Map<String, Object> application = new HashMap<>();
        try {
            application.put("liveness", LivenessState.CORRECT);
            application.put("readiness", ReadinessState.ACCEPTING_TRAFFIC);
            application.put("beansLoaded", applicationContext.getBeanDefinitionCount());
            log.info("✅ Application state: HEALTHY");
        } catch (Exception e) {
            application.put("status", "DEGRADED");
            application.put("error", e.getMessage());
            log.error("⚠️ Application state check failed", e);
        }
        response.put("application", application);

        // Overall status
        boolean isHealthy = database.get("status").equals("UP");
        response.put("status", isHealthy ? "UP" : "DOWN");

        log.info("🏥 Health check complete: {}", isHealthy ? "HEALTHY" : "UNHEALTHY");

        return isHealthy
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(503).body(response);
    }

    /**
     * Liveness probe - Kubernetes/Render style
     * Returns 200 if app is alive, 503 if it should be restarted
     */
    @GetMapping("/liveness")
    public ResponseEntity<Map<String, String>> liveness() {
        log.debug("Liveness probe requested");

        Map<String, String> response = new HashMap<>();
        response.put("status", "alive");
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe - indicates if app can handle traffic
     * Returns 200 if ready, 503 if not ready
     */
    @GetMapping("/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        log.debug("Readiness probe requested");

        Map<String, Object> response = new HashMap<>();
        boolean ready = true;
        String reason = "ready";

        // Check if database is accessible
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result == null || result != 1) {
                ready = false;
                reason = "database_connection_failed";
            }
        } catch (Exception e) {
            ready = false;
            reason = "database_unreachable: " + e.getMessage();
            log.warn("⚠️ Readiness check failed: database unreachable", e);
        }

        response.put("status", ready ? "ready" : "not_ready");
        response.put("reason", reason);
        response.put("timestamp", Instant.now().toString());

        log.debug("Readiness: {} ({})", ready ? "READY" : "NOT READY", reason);

        return ready
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(503).body(response);
    }
}

