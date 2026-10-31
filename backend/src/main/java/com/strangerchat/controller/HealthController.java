package com.strangerchat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Service health check for Render and monitoring")
public class HealthController {

    private final Instant startTime = Instant.now();

    @GetMapping
    @Operation(summary = "Health check endpoint for Render health checks and container orchestrators")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "strangerchat-backend",
                "timestamp", Instant.now().toString(),
                "uptimeSeconds", Instant.now().getEpochSecond() - startTime.getEpochSecond()
        ));
    }
}

