package com.example.distributedratelimiter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.distributedratelimiter.dto.RateLimitDecisionRequest;
import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.RateLimitContext;
import com.example.distributedratelimiter.model.RateLimitDecision;
import com.example.distributedratelimiter.model.RateLimitRule;
import com.example.distributedratelimiter.model.RuleEvaluation;
import com.example.distributedratelimiter.model.ScopeType;
import com.example.distributedratelimiter.model.TimeUnitType;
import com.example.distributedratelimiter.repository.RateLimitRuleRepository;
import com.example.distributedratelimiter.strategy.RateLimiterStrategy;
import com.example.distributedratelimiter.strategy.RateLimiterStrategyRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultRateLimitServiceTest {

    @Mock
    private RateLimitRuleRepository ruleRepository;

    private DefaultRateLimitService service;
    private RateLimiterStrategyRegistry strategyRegistry;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T00:00:00Z"), ZoneOffset.UTC);
        strategyRegistry = new RateLimiterStrategyRegistry(List.of(
                new StubRateLimiterStrategy(
                        AlgorithmType.TOKEN_BUCKET,
                        Map.of("rule-user", new RuleEvaluation(
                                "rule-user",
                                AlgorithmType.TOKEN_BUCKET,
                                ScopeType.USER_ID,
                                "*",
                                true,
                                8,
                                0,
                                true
                        ))
                ),
                new StubRateLimiterStrategy(
                        AlgorithmType.FIXED_WINDOW_COUNTER,
                        Map.of("rule-ip", new RuleEvaluation(
                                "rule-ip",
                                AlgorithmType.FIXED_WINDOW_COUNTER,
                                ScopeType.IP_ADDRESS,
                                "*",
                                false,
                                0,
                                12,
                                false
                        ))
                )
        ));
        service = new DefaultRateLimitService(ruleRepository, strategyRegistry, clock);
    }

    @Test
    void consumeShouldAggregateRuleResultsAndBlockWhenAnyRuleBlocks() {
        RateLimitRule userRule = new RateLimitRule(
                "rule-user",
                "user limit",
                AlgorithmType.TOKEN_BUCKET,
                ScopeType.USER_ID,
                "*",
                10,
                1,
                TimeUnitType.SECOND,
                20L,
                Instant.now(),
                Instant.now()
        );
        RateLimitRule ipRule = new RateLimitRule(
                "rule-ip",
                "ip limit",
                AlgorithmType.FIXED_WINDOW_COUNTER,
                ScopeType.IP_ADDRESS,
                "*",
                100,
                1,
                TimeUnitType.MINUTE,
                null,
                Instant.now(),
                Instant.now()
        );

        when(ruleRepository.findAll()).thenReturn(List.of(userRule, ipRule));

        RateLimitDecision decision = service.consume(new RateLimitDecisionRequest(
                "user-1",
                "203.0.113.10",
                "/api/orders",
                "req-1"
        ));

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.remainingQuota()).isEqualTo(0);
        assertThat(decision.retryAfterSeconds()).isEqualTo(12);
        assertThat(decision.evaluations()).hasSize(2);
    }

    @Test
    void checkShouldAllowWhenNoRuleMatchesTheRequest() {
        RateLimitRule endpointRule = new RateLimitRule(
                "rule-endpoint",
                "endpoint limit",
                AlgorithmType.SLIDING_WINDOW_LOG,
                ScopeType.ENDPOINT,
                "/internal-only",
                5,
                1,
                TimeUnitType.SECOND,
                null,
                Instant.now(),
                Instant.now()
        );

        when(ruleRepository.findAll()).thenReturn(List.of(endpointRule));

        RateLimitDecision decision = service.check(new RateLimitDecisionRequest(
                "user-1",
                "203.0.113.10",
                "/api/orders",
                null
        ));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.remainingQuota()).isNull();
        assertThat(decision.evaluations()).isEmpty();
    }

    private static final class StubRateLimiterStrategy implements RateLimiterStrategy {

        private final AlgorithmType algorithmType;
        private final Map<String, RuleEvaluation> evaluationsByRuleId;

        private StubRateLimiterStrategy(AlgorithmType algorithmType, Map<String, RuleEvaluation> evaluationsByRuleId) {
            this.algorithmType = algorithmType;
            this.evaluationsByRuleId = evaluationsByRuleId;
        }

        @Override
        public AlgorithmType algorithm() {
            return algorithmType;
        }

        @Override
        public RuleEvaluation evaluate(RateLimitRule rule, RateLimitContext context, boolean consume) {
            return evaluationsByRuleId.get(rule.id());
        }
    }
}
