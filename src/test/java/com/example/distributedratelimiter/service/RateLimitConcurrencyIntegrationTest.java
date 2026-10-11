package com.example.distributedratelimiter.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.distributedratelimiter.dto.CreateRateLimitRuleRequest;
import com.example.distributedratelimiter.dto.RateLimitDecisionRequest;
import com.example.distributedratelimiter.model.AlgorithmType;
import com.example.distributedratelimiter.model.RateLimitDecision;
import com.example.distributedratelimiter.model.ScopeType;
import com.example.distributedratelimiter.model.TimeUnitType;
import com.example.distributedratelimiter.test.RedisIntegrationTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RateLimitConcurrencyIntegrationTest extends RedisIntegrationTestSupport {

    @Autowired
    private RateLimitRuleService rateLimitRuleService;

    @Autowired
    private RateLimitService rateLimitService;

    @Test
    void fixedWindowCounterShouldNotOversellQuotaUnderConcurrentLoad() throws Exception {
        rateLimitRuleService.createRule(new CreateRateLimitRuleRequest(
                "concurrent-fixed-window-user",
                AlgorithmType.FIXED_WINDOW_COUNTER,
                ScopeType.USER_ID,
                "concurrent-user-fixed",
                10L,
                1L,
                TimeUnitType.MINUTE,
                null
        ));

        List<RateLimitDecision> decisions = executeConcurrently(IntStream.range(0, 100)
                .mapToObj(index -> (Callable<RateLimitDecision>) () -> rateLimitService.consume(new RateLimitDecisionRequest(
                        "concurrent-user-fixed",
                        "203.0.113.10",
                        "/api/concurrency/fixed-window",
                        "fixed-" + index
                )))
                .toList());

        assertThat(decisions).hasSize(100);
        assertThat(countAllowed(decisions)).isEqualTo(10);
        assertThat(countBlocked(decisions)).isEqualTo(90);
        assertThat(decisions.stream()
                .filter(decision -> !decision.allowed())
                .allMatch(decision -> decision.retryAfterSeconds() > 0)).isTrue();
    }

    @Test
    void tokenBucketShouldRespectBurstCapacityUnderConcurrentLoad() throws Exception {
        rateLimitRuleService.createRule(new CreateRateLimitRuleRequest(
                "concurrent-token-bucket-user",
                AlgorithmType.TOKEN_BUCKET,
                ScopeType.USER_ID,
                "concurrent-user-token",
                20L,
                1L,
                TimeUnitType.HOUR,
                20L
        ));

        List<RateLimitDecision> decisions = executeConcurrently(IntStream.range(0, 50)
                .mapToObj(index -> (Callable<RateLimitDecision>) () -> rateLimitService.consume(new RateLimitDecisionRequest(
                        "concurrent-user-token",
                        "203.0.113.11",
                        "/api/concurrency/token-bucket",
                        "token-" + index
                )))
                .toList());

        assertThat(decisions).hasSize(50);
        assertThat(countAllowed(decisions)).isEqualTo(20);
        assertThat(countBlocked(decisions)).isEqualTo(30);
    }

    @Test
    void slidingWindowShouldNotAllowMoreThanWindowLimitUnderConcurrentLoad() throws Exception {
        rateLimitRuleService.createRule(new CreateRateLimitRuleRequest(
                "concurrent-sliding-window-endpoint",
                AlgorithmType.SLIDING_WINDOW_LOG,
                ScopeType.ENDPOINT,
                "/api/concurrency/sliding-window",
                12L,
                1L,
                TimeUnitType.MINUTE,
                null
        ));

        List<RateLimitDecision> decisions = executeConcurrently(IntStream.range(0, 60)
                .mapToObj(index -> (Callable<RateLimitDecision>) () -> rateLimitService.consume(new RateLimitDecisionRequest(
                        "concurrent-user-sliding",
                        "203.0.113.12",
                        "/api/concurrency/sliding-window",
                        "sliding-" + index
                )))
                .toList());

        assertThat(decisions).hasSize(60);
        assertThat(countAllowed(decisions)).isEqualTo(12);
        assertThat(countBlocked(decisions)).isEqualTo(48);
    }

    @Test
    void wildcardUserRulesShouldKeepDifferentUsersIsolatedUnderConcurrentLoad() throws Exception {
        rateLimitRuleService.createRule(new CreateRateLimitRuleRequest(
                "concurrent-wildcard-user-isolation",
                AlgorithmType.FIXED_WINDOW_COUNTER,
                ScopeType.USER_ID,
                "*",
                5L,
                1L,
                TimeUnitType.MINUTE,
                null
        ));

        List<UserDecision> decisions = executeConcurrently(Stream.concat(
                        IntStream.range(0, 8).mapToObj(index -> decisionTask("user-a", index)),
                        IntStream.range(0, 8).mapToObj(index -> decisionTask("user-b", index))
                )
                .toList());

        long allowedForUserA = decisions.stream()
                .filter(decision -> decision.userId().equals("user-a"))
                .filter(decision -> decision.decision().allowed())
                .count();
        long allowedForUserB = decisions.stream()
                .filter(decision -> decision.userId().equals("user-b"))
                .filter(decision -> decision.decision().allowed())
                .count();

        assertThat(allowedForUserA).isEqualTo(5);
        assertThat(allowedForUserB).isEqualTo(5);
    }

    private Callable<UserDecision> decisionTask(String userId, int index) {
        return () -> new UserDecision(
                userId,
                rateLimitService.consume(new RateLimitDecisionRequest(
                        userId,
                        "198.51.100." + index,
                        "/api/concurrency/isolation",
                        userId + "-" + index
                ))
        );
    }

    private long countAllowed(List<RateLimitDecision> decisions) {
        return decisions.stream().filter(RateLimitDecision::allowed).count();
    }

    private long countBlocked(List<RateLimitDecision> decisions) {
        return decisions.stream().filter(decision -> !decision.allowed()).count();
    }

    private <T> List<T> executeConcurrently(List<Callable<T>> tasks) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(executorService.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out waiting to start concurrent tasks");
                    }
                    return task.call();
                }));
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executorService.shutdownNow();
        }
    }

    private record UserDecision(String userId, RateLimitDecision decision) {
    }
}
