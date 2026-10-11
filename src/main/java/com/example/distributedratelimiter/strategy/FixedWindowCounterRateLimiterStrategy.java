package com.example.distributedratelimiter.strategy;

import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.RateLimitContext;
import com.example.distributedratelimiter.model.RateLimitRule;
import com.example.distributedratelimiter.model.RuleEvaluation;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class FixedWindowCounterRateLimiterStrategy extends AbstractRedisRateLimiterStrategy {

    private final RedisScript<List> fixedWindowCounterScript;

    public FixedWindowCounterRateLimiterStrategy(
            StringRedisTemplate redisTemplate,
            Clock clock,
            @Qualifier("fixedWindowCounterScript") RedisScript<List> fixedWindowCounterScript
    ) {
        super(redisTemplate, clock);
        this.fixedWindowCounterScript = fixedWindowCounterScript;
    }

    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.FIXED_WINDOW_COUNTER;
    }

    @Override
    public RuleEvaluation evaluate(RateLimitRule rule, RateLimitContext context, boolean consume) {
        String identity = resolveIdentity(rule, context);
        List<?> result = executeScript(
                fixedWindowCounterScript,
                List.of(buildKey("fixed-window", rule, identity)),
                rule.limit(),
                rule.windowMillis(),
                nowMillis(),
                consume ? 1 : 0
        );
        return toEvaluation(rule, consume, result);
    }
}
