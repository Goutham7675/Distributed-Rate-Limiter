package com.example.distributedratelimiter.dto;

import com.example.distributedratelimiter.model.RateLimitDecision;
import java.util.List;

public record RateLimitDecisionResponse(
        boolean allowed,
        Long remainingQuota,
        long retryAfterSeconds,
        List<RuleEvaluationResponse> appliedRules
) {

    public static RateLimitDecisionResponse from(RateLimitDecision decision) {
        List<RuleEvaluationResponse> appliedRules = decision.evaluations().stream()
                .map(RuleEvaluationResponse::from)
                .toList();

        return new RateLimitDecisionResponse(
                decision.allowed(),
                decision.remainingQuota(),
                decision.retryAfterSeconds(),
                appliedRules
        );
    }
}
