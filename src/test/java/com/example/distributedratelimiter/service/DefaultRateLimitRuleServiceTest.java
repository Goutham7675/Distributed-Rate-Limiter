package com.example.distributedratelimiter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.distributedratelimiter.dto.CreateRateLimitRuleRequest;
import com.example.distributedratelimiter.exception.InvalidRequestException;
import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.RateLimitRule;
import com.example.distributedratelimiter.model.ScopeType;
import com.example.distributedratelimiter.model.TimeUnitType;
import com.example.distributedratelimiter.repository.RateLimitRuleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultRateLimitRuleServiceTest {

    @Mock
    private RateLimitRuleRepository ruleRepository;

    private DefaultRateLimitRuleService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T00:00:00Z"), ZoneOffset.UTC);
        service = new DefaultRateLimitRuleService(ruleRepository, clock);
    }

    @Test
    void createRuleShouldDefaultWildcardScopeAndBurstCapacity() {
        CreateRateLimitRuleRequest request = new CreateRateLimitRuleRequest(
                "user-bucket",
                AlgorithmType.TOKEN_BUCKET,
                ScopeType.USER_ID,
                null,
                10L,
                1L,
                TimeUnitType.SECOND,
                null
        );

        when(ruleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RateLimitRule created = service.createRule(request);

        ArgumentCaptor<RateLimitRule> captor = ArgumentCaptor.forClass(RateLimitRule.class);
        verify(ruleRepository).save(captor.capture());
        RateLimitRule saved = captor.getValue();

        assertThat(created.id()).isNotBlank();
        assertThat(saved.effectiveScopeValue()).isEqualTo("*");
        assertThat(saved.effectiveBurstCapacity()).isEqualTo(10L);
        assertThat(saved.createdAt()).isEqualTo(Instant.parse("2026-03-13T00:00:00Z"));
    }

    @Test
    void createRuleShouldRejectBurstCapacityForNonTokenBucketRules() {
        CreateRateLimitRuleRequest request = new CreateRateLimitRuleRequest(
                "ip-window",
                AlgorithmType.FIXED_WINDOW_COUNTER,
                ScopeType.IP_ADDRESS,
                "*",
                100L,
                1L,
                TimeUnitType.MINUTE,
                200L
        );

        assertThatThrownBy(() -> service.createRule(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("burstCapacity");
    }
}
