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
public class TokenBucketRateLimiterStrategy extends AbstractRedisRateLimiterStrategy {

    private final RedisScript<List> tokenBucketScript;

    public TokenBucketRateLimiterStrategy(
            StringRedisTemplate redisTemplate,
            Clock clock,
            @Qualifier("tokenBucketScript") RedisScript<List> tokenBucketScript
    ) {
        super(redisTemplate, clock);
        this.tokenBucketScript = tokenBucketScript;
    }

    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.TOKEN_BUCKET;
    }

    @Override
    public RuleEvaluation evaluate(RateLimitRule rule, RateLimitContext context, boolean consume) {
        String identity = resolveIdentity(rule, context);
        List<?> result = executeScript(
                tokenBucketScript,
                List.of(buildKey("token-bucket", rule, identity)),
                rule.effectiveBurstCapacity(),
                rule.limit(),
                rule.windowMillis(),
                nowMillis(),
                1,
                consume ? 1 : 0
        );
        return toEvaluation(rule, consume, result);
    }
}
