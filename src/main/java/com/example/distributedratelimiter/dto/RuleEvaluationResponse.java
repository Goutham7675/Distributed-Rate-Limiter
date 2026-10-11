package com.example.distributedratelimiter.dto;

import com.example.distributedratelimiter.model.RuleEvaluation;

public record RuleEvaluationResponse(
        String ruleId,
        String algorithm,
        String scopeType,
        String scopeValue,
        boolean allowed,
        long remainingQuota,
        long retryAfterSeconds,
        boolean consumed
) {

    public static RuleEvaluationResponse from(RuleEvaluation evaluation) {
        return new RuleEvaluationResponse(
                evaluation.ruleId(),
                evaluation.algorithm().name(),
                evaluation.scopeType().name(),
                evaluation.scopeValue(),
                evaluation.allowed(),
                evaluation.remainingQuota(),
                evaluation.retryAfterSeconds(),
                evaluation.consumed()
        );
    }
}
