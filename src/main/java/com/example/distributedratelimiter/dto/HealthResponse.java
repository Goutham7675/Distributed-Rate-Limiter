package com.example.distributedratelimiter.dto;

import java.time.Instant;

public record HealthResponse(
        String status,
        String redis,
        Instant timestamp
) {
}
