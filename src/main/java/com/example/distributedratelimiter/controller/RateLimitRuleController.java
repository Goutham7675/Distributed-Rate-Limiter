package com.example.distributedratelimiter.controller;

import com.example.distributedratelimiter.dto.CreateRateLimitRuleRequest;
import com.example.distributedratelimiter.dto.RateLimitRuleResponse;
import com.example.distributedratelimiter.service.RateLimitRuleService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rules")
public class RateLimitRuleController {

    private static final Logger log = LoggerFactory.getLogger(RateLimitRuleController.class);

    private final RateLimitRuleService rateLimitRuleService;

    public RateLimitRuleController(RateLimitRuleService rateLimitRuleService) {
        this.rateLimitRuleService = rateLimitRuleService;
    }

    @PostMapping
    public ResponseEntity<RateLimitRuleResponse> createRule(@Valid @RequestBody CreateRateLimitRuleRequest request) {
        log.info("Received create rule request for {}", request.name());
        RateLimitRuleResponse response = RateLimitRuleResponse.from(rateLimitRuleService.createRule(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<RateLimitRuleResponse> getRules() {
        return rateLimitRuleService.getRules()
                .stream()
                .map(RateLimitRuleResponse::from)
                .toList();
    }
}
