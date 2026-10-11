package com.example.distributedratelimiter.model;

import java.time.Instant;

public record RateLimitRule(
        String id,
        String name,
        AlgorithmType algorithm,
        ScopeType scopeType,
        String scopeValue,
        long limit,
        long windowSize,
        TimeUnitType windowUnit,
        Long burstCapacity,
        Instant createdAt,
        Instant updatedAt
) {

    public String effectiveScopeValue() {
        if (scopeValue == null || scopeValue.isBlank()) {
            return "*";
        }
        return scopeValue.trim();
    }

    public boolean appliesTo(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return "*".equals(effectiveScopeValue()) || effectiveScopeValue().equals(candidate);
    }

    public long windowMillis() {
        return windowUnit.toMillis(windowSize);
    }

    public long effectiveBurstCapacity() {
        return burstCapacity == null ? limit : burstCapacity;
    }
}
