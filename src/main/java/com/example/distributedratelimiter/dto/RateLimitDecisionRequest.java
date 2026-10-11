package com.example.distributedratelimiter.dto;

import jakarta.validation.constraints.Size;

public record RateLimitDecisionRequest(
        @Size(max = 128, message = "userId must be 128 characters or fewer")
        String userId,
        @Size(max = 64, message = "ipAddress must be 64 characters or fewer")
        String ipAddress,
        @Size(max = 256, message = "endpoint must be 256 characters or fewer")
        String endpoint,
        @Size(max = 128, message = "requestId must be 128 characters or fewer")
        String requestId
) {
}
