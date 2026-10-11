package com.example.distributedratelimiter.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.distributedratelimiter.dto.CreateRateLimitRuleRequest;
import com.example.distributedratelimiter.dto.RateLimitDecisionRequest;
import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.ScopeType;
import com.example.distributedratelimiter.model.TimeUnitType;
import com.example.distributedratelimiter.test.RedisIntegrationTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class RateLimitControllerIntegrationTest extends RedisIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fixedWindowCounterShouldBlockThirdRequestInSameWindow() throws Exception {
        createRule(new CreateRateLimitRuleRequest(
                "fixed-window-user",
                AlgorithmType.FIXED_WINDOW_COUNTER,
                ScopeType.USER_ID,
                "*",
                2L,
                1L,
                TimeUnitType.SECOND,
                null
        ));

        RateLimitDecisionRequest request = new RateLimitDecisionRequest(
                "user-123",
                "203.0.113.10",
                "/api/orders",
                null
        );

        consume(request, true, 1);
        consume(request, true, 0);
        consumeBlocked(request);
    }

    @Test
    void slidingWindowLogShouldAllowAgainAfterWindowExpires() throws Exception {
        createRule(new CreateRateLimitRuleRequest(
                "sliding-endpoint",
                AlgorithmType.SLIDING_WINDOW_LOG,
                ScopeType.ENDPOINT,
                "/api/orders",
                2L,
                1L,
                TimeUnitType.SECOND,
                null
        ));

        RateLimitDecisionRequest request = new RateLimitDecisionRequest(
                "user-456",
                "203.0.113.11",
                "/api/orders",
                null
        );

        consume(request, true, 1);
        consume(request, true, 0);
        consumeBlocked(request);

        TimeUnit.MILLISECONDS.sleep(1_100);

        consume(request, true, 1);
    }

    @Test
    void tokenBucketShouldRefillAfterPause() throws Exception {
        createRule(new CreateRateLimitRuleRequest(
                "token-ip",
                AlgorithmType.TOKEN_BUCKET,
                ScopeType.IP_ADDRESS,
                "*",
                2L,
                1L,
                TimeUnitType.SECOND,
                2L
        ));

        RateLimitDecisionRequest request = new RateLimitDecisionRequest(
                "user-789",
                "203.0.113.15",
                "/api/payments",
                null
        );

        consume(request, true, 1);
        consume(request, true, 0);
        consumeBlocked(request);

        TimeUnit.MILLISECONDS.sleep(1_100);

        consume(request, true, 1);
    }

    private void createRule(CreateRateLimitRuleRequest request) throws Exception {
        mockMvc.perform(post("/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private void consume(RateLimitDecisionRequest request, boolean expectedAllowed, int expectedRemaining) throws Exception {
        mockMvc.perform(post("/consume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(expectedAllowed))
                .andExpect(jsonPath("$.remainingQuota").value(expectedRemaining));
    }

    private void consumeBlocked(RateLimitDecisionRequest request) throws Exception {
        mockMvc.perform(post("/consume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.remainingQuota").value(0))
                .andExpect(jsonPath("$.retryAfterSeconds").value(greaterThanOrEqualTo(1)));
    }
}
