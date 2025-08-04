package com.back.back9.global.redis.initializer;

import com.back.back9.domain.websocket.service.UpbitRestCandleFetcher;
import com.back.back9.domain.websocket.service.UpbitWebSocketConnector;
import com.back.back9.domain.websocket.vo.CandleInterval;
import com.back.back9.global.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.EnumMap;
import java.util.Map;

import static com.back.back9.domain.websocket.vo.CandleInterval.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisInitializer {

    private final RedisService redisService;
    private final UpbitRestCandleFetcher fetcher;
    private final UpbitWebSocketConnector webSocketConnector;

    private static final int FETCH_BATCH_SIZE = 200;
    private static final long RATE_LIMIT_DELAY_MS = 3 * 60 * 1000; // 3분
    private static final long BETWEEN_CALL_DELAY_MS = 200;

    private final Map<CandleInterval, Integer> intervalTargetCount = new EnumMap<>(Map.of(
            SEC, 1000,
            MIN_1, 1000,
            MIN_30, 1000,
            HOUR_1, 1000,
            DAY, 500,
            WEEK, 400,    // 200씩 2번
            MONTH, 200,   // 1번
            YEAR, 200     // 1번
    ));

    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup() {
        log.info("✅ Spring Boot 부팅 완료, WebSocket 연결 시작");

        redisService.clearAll();
        webSocketConnector.connect();

        new Thread(this::initializeCandles).start();
    }

    private void initializeCandles() {
        log.info("🕒 백그라운드에서 캔들 초기화 시작");

        for (Map.Entry<CandleInterval, Integer> entry : intervalTargetCount.entrySet()) {
            CandleInterval interval = entry.getKey();
            int target = entry.getValue();
            int fetched = 0;

            while (fetched < target) {
                try {
                    int count = Math.min(FETCH_BATCH_SIZE, target - fetched);
                    fetcher.fetchUntil(interval, count);
                    fetched += count;
                    Thread.sleep(BETWEEN_CALL_DELAY_MS);
                } catch (HttpClientErrorException.TooManyRequests e) {
                    log.warn("⏸️ 429 Too Many Requests 발생: 3분간 전체 수집 중단 후 재시도");
                    sleep();
                } catch (Exception e) {
                    log.error("❌ {} 캔들 수집 중 오류: {}", interval.name(), e.getMessage());
                    return;
                }
            }

            log.info("✅ {} 캔들 {}개 등록 완료 (목표: {})", interval.name(), fetched, target);
        }

        log.info("🎉 전체 캔들 초기화 완료");
    }

    private void sleep() {
        try {
            Thread.sleep(RedisInitializer.RATE_LIMIT_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}