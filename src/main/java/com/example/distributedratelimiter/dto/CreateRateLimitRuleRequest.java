package com.example.distributedratelimiter.dto;

import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.ScopeType;
import com.example.distributedratelimiter.model.TimeUnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRateLimitRuleRequest(
        @NotBlank(message = "name is required")
        String name,
        @NotNull(message = "algorithm is required")
        AlgorithmType algorithm,
        @NotNull(message = "scopeType is required")
        ScopeType scopeType,
        String scopeValue,
        @NotNull(message = "limit is required")
        @Positive(message = "limit must be positive")
        Long limit,
        @NotNull(message = "windowSize is required")
        @Positive(message = "windowSize must be positive")
        Long windowSize,
        @NotNull(message = "windowUnit is required")
        TimeUnitType windowUnit,
        @Positive(message = "burstCapacity must be positive")
        Long burstCapacity
) {
}
