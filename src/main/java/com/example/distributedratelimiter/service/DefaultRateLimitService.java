package com.example.distributedratelimiter.service;

import com.example.distributedratelimiter.dto.RateLimitDecisionRequest;
import com.example.distributedratelimiter.exception.InvalidRequestException;
import com.example.distributedratelimiter.model.RateLimitContext;
import com.example.distributedratelimiter.model.RateLimitDecision;
import com.example.distributedratelimiter.model.RateLimitRule;
import com.example.distributedratelimiter.model.RuleEvaluation;
import com.example.distributedratelimiter.repository.RateLimitRuleRepository;
import com.example.distributedratelimiter.strategy.RateLimiterStrategy;
import com.example.distributedratelimiter.strategy.RateLimiterStrategyRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultRateLimitService implements RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(DefaultRateLimitService.class);

    private final RateLimitRuleRepository ruleRepository;
    private final RateLimiterStrategyRegistry strategyRegistry;
    private final Clock clock;

    public DefaultRateLimitService(
            RateLimitRuleRepository ruleRepository,
            RateLimiterStrategyRegistry strategyRegistry,
            Clock clock
    ) {
        this.ruleRepository = ruleRepository;
        this.strategyRegistry = strategyRegistry;
        this.clock = clock;
    }

    @Override
    public RateLimitDecision check(RateLimitDecisionRequest request) {
        return evaluate(request, false);
    }

    @Override
    public RateLimitDecision consume(RateLimitDecisionRequest request) {
        return evaluate(request, true);
    }

    private RateLimitDecision evaluate(RateLimitDecisionRequest request, boolean consume) {
        validateRequest(request);

        RateLimitContext context = new RateLimitContext(
                resolveRequestId(request.requestId()),
                trimToNull(request.userId()),
                trimToNull(request.ipAddress()),
                trimToNull(request.endpoint()),
                Instant.now(clock)
        );

        // Matching happens in the service layer so controllers stay thin and new scopes can be added centrally.
        List<RuleEvaluation> evaluations = ruleRepository.findAll()
                .stream()
                .filter(rule -> isApplicable(rule, context))
                .map(rule -> evaluateRule(rule, context, consume))
                .collect(Collectors.toList());

        if (evaluations.isEmpty()) {
            log.info("No matching rules for request {}", context.requestId());
            return new RateLimitDecision(true, null, 0, List.of());
        }

        boolean allowed = evaluations.stream().allMatch(RuleEvaluation::allowed);
        Long remainingQuota = evaluations.stream()
                .map(RuleEvaluation::remainingQuota)
                .min(Long::compareTo)
                .orElse(null);
        long retryAfterSeconds = allowed
                ? 0
                : evaluations.stream()
                        .filter(evaluation -> !evaluation.allowed())
                        .mapToLong(RuleEvaluation::retryAfterSeconds)
                        .max()
                        .orElse(0);

        log.info(
                "Request {} evaluated against {} rules. allowed={}, consume={}",
                context.requestId(),
                evaluations.size(),
                allowed,
                consume
        );

        return new RateLimitDecision(allowed, remainingQuota, retryAfterSeconds, evaluations);
    }

    private RuleEvaluation evaluateRule(RateLimitRule rule, RateLimitContext context, boolean consume) {
        RateLimiterStrategy strategy = strategyRegistry.resolve(rule.algorithm());
        return strategy.evaluate(rule, context, consume);
    }

    private boolean isApplicable(RateLimitRule rule, RateLimitContext context) {
        String identity = trimToNull(context.identityFor(rule.scopeType()));
        return identity != null && rule.appliesTo(identity);
    }

    private void validateRequest(RateLimitDecisionRequest request) {
        if (trimToNull(request.userId()) == null
                && trimToNull(request.ipAddress()) == null
                && trimToNull(request.endpoint()) == null) {
            throw new InvalidRequestException("At least one of userId, ipAddress, or endpoint must be provided");
        }
    }

    private String resolveRequestId(String requestId) {
        String trimmed = trimToNull(requestId);
        return trimmed == null ? UUID.randomUUID().toString() : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
