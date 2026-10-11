package com.example.distributedratelimiter.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.distributedratelimiter.dto.CreateRateLimitRuleRequest;
import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.ScopeType;
import com.example.distributedratelimiter.model.TimeUnitType;
import com.example.distributedratelimiter.test.RedisIntegrationTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class RateLimitRuleControllerIntegrationTest extends RedisIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndListRules() throws Exception {
        CreateRateLimitRuleRequest request = new CreateRateLimitRuleRequest(
                "user-limit",
                AlgorithmType.TOKEN_BUCKET,
                ScopeType.USER_ID,
                "*",
                10L,
                1L,
                TimeUnitType.SECOND,
                20L
        );

        mockMvc.perform(post("/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("user-limit"))
                .andExpect(jsonPath("$.algorithm").value("TOKEN_BUCKET"))
                .andExpect(jsonPath("$.scopeType").value("USER_ID"))
                .andExpect(jsonPath("$.scopeValue").value("*"));

        mockMvc.perform(get("/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("user-limit"))
                .andExpect(jsonPath("$[0].burstCapacity").value(20));
    }
}
