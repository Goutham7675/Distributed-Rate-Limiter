package com.example.distributedratelimiter.service;

import com.example.distributedratelimiter.dto.RateLimitDecisionRequest;
import com.example.distributedratelimiter.model.RateLimitDecision;

public interface RateLimitService {

    RateLimitDecision check(RateLimitDecisionRequest request);

    RateLimitDecision consume(RateLimitDecisionRequest request);
}
