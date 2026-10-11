package com.example.distributedratelimiter.service;

import com.example.distributedratelimiter.dto.CreateRateLimitRuleRequest;
import com.example.distributedratelimiter.exception.InvalidRequestException;
import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.RateLimitRule;
import com.example.distributedratelimiter.repository.RateLimitRuleRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultRateLimitRuleService implements RateLimitRuleService {

    private static final Logger log = LoggerFactory.getLogger(DefaultRateLimitRuleService.class);

    private final RateLimitRuleRepository ruleRepository;
    private final Clock clock;

    public DefaultRateLimitRuleService(RateLimitRuleRepository ruleRepository, Clock clock) {
        this.ruleRepository = ruleRepository;
        this.clock = clock;
    }

    @Override
    public RateLimitRule createRule(CreateRateLimitRuleRequest request) {
        validateRequest(request);

        Instant now = Instant.now(clock);
        RateLimitRule rule = new RateLimitRule(
                UUID.randomUUID().toString(),
                request.name().trim(),
                request.algorithm(),
                request.scopeType(),
                normalizeScopeValue(request.scopeValue()),
                request.limit(),
                request.windowSize(),
                request.windowUnit(),
                request.algorithm() == AlgorithmType.TOKEN_BUCKET ? normalizeBurstCapacity(request) : null,
                now,
                now
        );

        log.info("Creating rule {} for scope {}", rule.id(), rule.scopeType());
        return ruleRepository.save(rule);
    }

    @Override
    public List<RateLimitRule> getRules() {
        return ruleRepository.findAll();
    }

    private void validateRequest(CreateRateLimitRuleRequest request) {
        if (request.algorithm() != AlgorithmType.TOKEN_BUCKET && request.burstCapacity() != null) {
            throw new InvalidRequestException("burstCapacity is only supported for TOKEN_BUCKET rules");
        }
    }

    private Long normalizeBurstCapacity(CreateRateLimitRuleRequest request) {
        return request.burstCapacity() == null ? request.limit() : request.burstCapacity();
    }

    private String normalizeScopeValue(String scopeValue) {
        if (scopeValue == null || scopeValue.isBlank()) {
            return "*";
        }
        return scopeValue.trim();
    }
}
