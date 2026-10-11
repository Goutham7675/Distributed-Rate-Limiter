package com.example.distributedratelimiter.controller;

import com.example.distributedratelimiter.dto.RateLimitDecisionRequest;
import com.example.distributedratelimiter.dto.RateLimitDecisionResponse;
import com.example.distributedratelimiter.service.RateLimitService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitController {

    private static final Logger log = LoggerFactory.getLogger(RateLimitController.class);

    private final RateLimitService rateLimitService;

    public RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/check")
    public RateLimitDecisionResponse check(@Valid @RequestBody RateLimitDecisionRequest request) {
        log.debug("Checking request eligibility");
        return RateLimitDecisionResponse.from(rateLimitService.check(request));
    }

    @PostMapping("/consume")
    public RateLimitDecisionResponse consume(@Valid @RequestBody RateLimitDecisionRequest request) {
        log.debug("Consuming request quota");
        return RateLimitDecisionResponse.from(rateLimitService.consume(request));
    }
}
