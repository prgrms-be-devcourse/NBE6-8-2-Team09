package com.back.back9.global.redis.initializer;

import com.back.back9.domain.websocket.service.UpbitRestCandleFetcher;
import com.back.back9.domain.websocket.vo.CandleInterval;
import com.back.back9.global.redis.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisInitializer {

    private final UpbitRestCandleFetcher restCandleFetcher;
    private final RedisService redisService;

    @PostConstruct
    public void initializeRedisWithInitialData() {
        redisService.clearAll();
        for (CandleInterval interval : CandleInterval.values()) {
            try {
                int count = interval.getMaxSize();
                log.info("초기 Redis 저장 시작: [{}], size={}", interval.getSuffix(), count);

                restCandleFetcher.fetchInterval(interval, count);

                Thread.sleep(200);
            } catch (Exception e) {
                log.error("초기 Redis 저장 실패: [{}] - {}", interval.getSuffix(), e.getMessage(), e);
            }
        }

        log.info("✅ Redis 초기화 완료");
    }
}