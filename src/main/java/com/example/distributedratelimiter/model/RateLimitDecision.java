package com.example.distributedratelimiter.model;

import java.util.List;

public record RateLimitDecision(
        boolean allowed,
        Long remainingQuota,
        long retryAfterSeconds,
        List<RuleEvaluation> evaluations
) {
}
