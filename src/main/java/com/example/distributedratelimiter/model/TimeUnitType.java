package com.example.distributedratelimiter.model;

import java.time.Duration;

public enum TimeUnitType {
    SECOND(Duration.ofSeconds(1)),
    MINUTE(Duration.ofMinutes(1)),
    HOUR(Duration.ofHours(1));

    private final Duration baseDuration;

    TimeUnitType(Duration baseDuration) {
        this.baseDuration = baseDuration;
    }

    public Duration toDuration(long size) {
        return baseDuration.multipliedBy(size);
    }

    public long toMillis(long size) {
        return toDuration(size).toMillis();
    }
}
