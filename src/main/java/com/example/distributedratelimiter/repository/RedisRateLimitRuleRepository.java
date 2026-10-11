package com.example.distributedratelimiter.repository;

import com.example.distributedratelimiter.exception.RulePersistenceException;
import com.example.distributedratelimiter.model.RateLimitRule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRateLimitRuleRepository implements RateLimitRuleRepository {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitRuleRepository.class);
    private static final String RULES_KEY = "rate-limit:rules";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRateLimitRuleRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RateLimitRule save(RateLimitRule rule) {
        try {
            HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
            hashOperations.put(RULES_KEY, rule.id(), objectMapper.writeValueAsString(rule));
            log.info("Stored rate limit rule {}", rule.id());
            return rule;
        } catch (JsonProcessingException | DataAccessException exception) {
            throw new RulePersistenceException("Failed to save rate limit rule", exception);
        }
    }

    @Override
    public List<RateLimitRule> findAll() {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(RULES_KEY);
            List<RateLimitRule> rules = new ArrayList<>();
            for (Object value : entries.values()) {
                rules.add(deserialize(String.valueOf(value)));
            }
            rules.sort(Comparator.comparing(RateLimitRule::createdAt));
            return rules;
        } catch (DataAccessException exception) {
            throw new RulePersistenceException("Failed to fetch rate limit rules", exception);
        }
    }

    @Override
    public Optional<RateLimitRule> findById(String id) {
        try {
            Object value = redisTemplate.opsForHash().get(RULES_KEY, id);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(deserialize(String.valueOf(value)));
        } catch (DataAccessException exception) {
            throw new RulePersistenceException("Failed to fetch rate limit rule", exception);
        }
    }

    private RateLimitRule deserialize(String value) {
        try {
            return objectMapper.readValue(value, RateLimitRule.class);
        } catch (JsonProcessingException exception) {
            throw new RulePersistenceException("Failed to deserialize rate limit rule", exception);
        }
    }
}
