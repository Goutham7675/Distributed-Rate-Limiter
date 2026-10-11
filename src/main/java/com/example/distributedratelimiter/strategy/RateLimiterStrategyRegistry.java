package com.example.distributedratelimiter.strategy;

import com.example.distributedratelimiter.exception.UnsupportedAlgorithmException;
import com.example.distributedratelimiter.model.AlgorithmType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterStrategyRegistry {

    private final Map<AlgorithmType, RateLimiterStrategy> strategies;

    public RateLimiterStrategyRegistry(List<RateLimiterStrategy> strategies) {
        this.strategies = new EnumMap<>(AlgorithmType.class);
        for (RateLimiterStrategy strategy : strategies) {
            this.strategies.put(strategy.algorithm(), strategy);
        }
    }

    public RateLimiterStrategy resolve(AlgorithmType algorithm) {
        RateLimiterStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new UnsupportedAlgorithmException("No strategy registered for algorithm " + algorithm);
        }
        return strategy;
    }
}
