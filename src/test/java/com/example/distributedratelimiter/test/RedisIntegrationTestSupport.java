package com.example.distributedratelimiter.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class RedisIntegrationTestSupport {

    private static final Logger log = LoggerFactory.getLogger(RedisIntegrationTestSupport.class);
    private static final int TEST_REDIS_DATABASE = 14;
    private static final String LOCAL_REDIS_HOST = "127.0.0.1";
    private static final int LOCAL_REDIS_PORT = 6379;

    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);
    private static final RedisConnectionSettings REDIS_CONNECTION_SETTINGS = resolveRedisConnectionSettings();

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONNECTION_SETTINGS::host);
        registry.add("spring.data.redis.port", REDIS_CONNECTION_SETTINGS::port);
        registry.add("spring.data.redis.database", () -> TEST_REDIS_DATABASE);
    }

    @BeforeEach
    void flushRedis() {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    private static RedisConnectionSettings resolveRedisConnectionSettings() {
        if (isLocalRedisAvailable()) {
            log.info(
                    "Using local Redis {}:{} for integration tests on database {}",
                    LOCAL_REDIS_HOST,
                    LOCAL_REDIS_PORT,
                    TEST_REDIS_DATABASE
            );
            return new RedisConnectionSettings(LOCAL_REDIS_HOST, LOCAL_REDIS_PORT);
        }

        try {
            REDIS.start();
            log.info(
                    "Using Testcontainers Redis {}:{} for integration tests on database {}",
                    REDIS.getHost(),
                    REDIS.getFirstMappedPort(),
                    TEST_REDIS_DATABASE
            );
            return new RedisConnectionSettings(REDIS.getHost(), REDIS.getFirstMappedPort());
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No Redis test backend available. Start local Redis on localhost:6379 or enable Docker for Testcontainers.",
                    exception
            );
        }
    }

    private static boolean isLocalRedisAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(LOCAL_REDIS_HOST, LOCAL_REDIS_PORT), 500);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private record RedisConnectionSettings(String host, int port) {
    }
}
