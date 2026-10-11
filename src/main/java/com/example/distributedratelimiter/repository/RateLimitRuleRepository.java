package com.example.distributedratelimiter.repository;

import com.example.distributedratelimiter.model.RateLimitRule;
import java.util.List;
import java.util.Optional;

public interface RateLimitRuleRepository {

    RateLimitRule save(RateLimitRule rule);

    List<RateLimitRule> findAll();

    Optional<RateLimitRule> findById(String id);
}
