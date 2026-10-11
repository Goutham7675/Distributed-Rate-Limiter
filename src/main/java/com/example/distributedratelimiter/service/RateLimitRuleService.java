package com.example.distributedratelimiter.service;

import com.example.distributedratelimiter.dto.CreateRateLimitRuleRequest;
import com.example.distributedratelimiter.model.RateLimitRule;
import java.util.List;

public interface RateLimitRuleService {

    RateLimitRule createRule(CreateRateLimitRuleRequest request);

    List<RateLimitRule> getRules();
}
