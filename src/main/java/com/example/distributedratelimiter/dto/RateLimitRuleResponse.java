package com.example.distributedratelimiter.dto;

import com.example.distributedratelimiter.model.RateLimitRule;
import java.time.Instant;

public record RateLimitRuleResponse(
        String id,
        String name,
        String algorithm,
        String scopeType,
        String scopeValue,
        long limit,
        long windowSize,
        String windowUnit,
        Long burstCapacity,
        Instant createdAt,
        Instant updatedAt
) {

    public static RateLimitRuleResponse from(RateLimitRule rule) {
        return new RateLimitRuleResponse(
                rule.id(),
                rule.name(),
                rule.algorithm().name(),
                rule.scopeType().name(),
                rule.effectiveScopeValue(),
                rule.limit(),
                rule.windowSize(),
                rule.windowUnit().name(),
                rule.burstCapacity(),
                rule.createdAt(),
                rule.updatedAt()
        );
    }
}
