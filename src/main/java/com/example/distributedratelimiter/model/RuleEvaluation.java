package com.example.distributedratelimiter.model;

public record RuleEvaluation(
        String ruleId,
        AlgorithmType algorithm,
        ScopeType scopeType,
        String scopeValue,
        boolean allowed,
        long remainingQuota,
        long retryAfterSeconds,
        boolean consumed
) {
}
