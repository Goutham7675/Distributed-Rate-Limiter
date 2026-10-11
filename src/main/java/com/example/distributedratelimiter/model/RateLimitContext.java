package com.example.distributedratelimiter.model;

import java.time.Instant;

public record RateLimitContext(
        String requestId,
        String userId,
        String ipAddress,
        String endpoint,
        Instant timestamp
) {

    public String identityFor(ScopeType scopeType) {
        return switch (scopeType) {
            case USER_ID -> userId;
            case IP_ADDRESS -> ipAddress;
            case ENDPOINT -> endpoint;
        };
    }
}
