package com.example.distributedratelimiter.strategy;

import com.example.distributedratelimiter.exception.InvalidRequestException;
import com.example.distributedratelimiter.exception.RulePersistenceException;
import com.example.distributedratelimiter.model.RateLimitContext;
import com.example.distributedratelimiter.model.RateLimitRule;
import com.example.distributedratelimiter.model.RuleEvaluation;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

public abstract class AbstractRedisRateLimiterStrategy implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    protected AbstractRedisRateLimiterStrategy(StringRedisTemplate redisTemplate, Clock clock) {
        this.redisTemplate = redisTemplate;
        this.clock = clock;
    }

    protected long nowMillis() {
        return clock.millis();
    }

    protected String resolveIdentity(RateLimitRule rule, RateLimitContext context) {
        String identity = context.identityFor(rule.scopeType());
        if (identity == null || identity.isBlank()) {
            throw new InvalidRequestException("Missing identity for scope " + rule.scopeType());
        }
        return identity;
    }

    protected String buildKey(String namespace, RateLimitRule rule, String identity) {
        return "rate-limit:" + namespace + ":" + rule.id() + ":" + encode(identity);
    }

    protected List<?> executeScript(RedisScript<List> script, List<String> keys, Object... args) {
        Object[] serializedArgs = new Object[args.length];
        for (int index = 0; index < args.length; index++) {
            serializedArgs[index] = String.valueOf(args[index]);
        }

        List<?> result = redisTemplate.execute(script, keys, serializedArgs);
        if (result == null || result.size() < 3) {
            throw new RulePersistenceException(
                    "Redis script returned an invalid result",
                    new IllegalStateException("Expected [allowed, remainingQuota, retryAfterMillis]")
            );
        }
        return result;
    }

    protected RuleEvaluation toEvaluation(RateLimitRule rule, boolean consume, List<?> result) {
        boolean allowed = parseLong(result.get(0)) == 1L;
        long remainingQuota = parseLong(result.get(1));
        long retryAfterMillis = parseLong(result.get(2));
        long retryAfterSeconds = retryAfterMillis <= 0 ? 0 : (retryAfterMillis + 999) / 1000;

        return new RuleEvaluation(
                rule.id(),
                rule.algorithm(),
                rule.scopeType(),
                rule.effectiveScopeValue(),
                allowed,
                remainingQuota,
                retryAfterSeconds,
                consume && allowed
        );
    }

    private long parseLong(Object value) {
        return Long.parseLong(String.valueOf(value));
    }

    private String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
