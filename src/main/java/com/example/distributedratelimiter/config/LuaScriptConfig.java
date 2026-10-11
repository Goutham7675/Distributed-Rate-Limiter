package com.example.distributedratelimiter.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class LuaScriptConfig {

    @Bean
    public RedisScript<List> tokenBucketScript() {
        return buildScript("lua/token-bucket.lua");
    }

    @Bean
    public RedisScript<List> fixedWindowCounterScript() {
        return buildScript("lua/fixed-window-counter.lua");
    }

    @Bean
    public RedisScript<List> slidingWindowLogScript() {
        return buildScript("lua/sliding-window-log.lua");
    }

    private RedisScript<List> buildScript(String path) {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(List.class);
        return script;
    }
}
