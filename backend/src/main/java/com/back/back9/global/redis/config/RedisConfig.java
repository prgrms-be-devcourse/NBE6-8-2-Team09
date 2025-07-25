package com.back.back9.global.redis.config;

import com.back.back9.global.redis.initializer.RedisInitializer;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    private final RedisInitializer redisInitializer;

    public RedisConfig(RedisInitializer redisInitializer) {
        this.redisInitializer = redisInitializer;
    }

    @PostConstruct
    public void initializeRedis() {
        redisInitializer.initialize(); // 초기화 한 번만 실행
    }
}