package com.example.distributedratelimiter.strategy;

import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.RateLimitContext;
import com.example.distributedratelimiter.model.RateLimitRule;
import com.example.distributedratelimiter.model.RuleEvaluation;

public interface RateLimiterStrategy {

    AlgorithmType algorithm();

    RuleEvaluation evaluate(RateLimitRule rule, RateLimitContext context, boolean consume);
}
