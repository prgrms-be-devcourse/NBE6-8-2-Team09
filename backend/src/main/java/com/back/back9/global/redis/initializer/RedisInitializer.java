package com.back.back9.global.redis.initializer;

import com.back.back9.global.redis.service.RedisService;
import org.springframework.stereotype.Component;

@Component
public class RedisInitializer {

    private final RedisService redisService;

    public RedisInitializer(RedisService redisService) {
        this.redisService = redisService;
    }

    public void initialize() {
        System.out.println("Redis 초기화 시작...");
        redisService.initRedis();
        System.out.println("Redis 초기화 완료");
    }
}