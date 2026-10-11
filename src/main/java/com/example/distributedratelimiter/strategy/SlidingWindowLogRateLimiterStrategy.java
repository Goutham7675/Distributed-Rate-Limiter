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
public class SlidingWindowLogRateLimiterStrategy extends AbstractRedisRateLimiterStrategy {

    private final RedisScript<List> slidingWindowLogScript;

    public SlidingWindowLogRateLimiterStrategy(
            StringRedisTemplate redisTemplate,
            Clock clock,
            @Qualifier("slidingWindowLogScript") RedisScript<List> slidingWindowLogScript
    ) {
        super(redisTemplate, clock);
        this.slidingWindowLogScript = slidingWindowLogScript;
    }

    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SLIDING_WINDOW_LOG;
    }

    @Override
    public RuleEvaluation evaluate(RateLimitRule rule, RateLimitContext context, boolean consume) {
        String identity = resolveIdentity(rule, context);
        List<?> result = executeScript(
                slidingWindowLogScript,
                List.of(buildKey("sliding-window", rule, identity)),
                rule.limit(),
                rule.windowMillis(),
                nowMillis(),
                context.requestId(),
                consume ? 1 : 0
        );
        return toEvaluation(rule, consume, result);
    }
}
