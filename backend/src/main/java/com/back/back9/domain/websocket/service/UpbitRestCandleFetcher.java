package com.back.back9.domain.websocket.service;

import com.back.back9.domain.websocket.mock.MockCoinListProvider;
import com.back.back9.domain.websocket.vo.CandleInterval;
import com.back.back9.global.redis.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UpbitRestCandleFetcher {

    private final RedisService redisService;
    private final MockCoinListProvider coinListProvider;
    private final RestTemplate rest = new RestTemplate();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_PER_REQUEST = 200;

    public int fetchInterval(CandleInterval interval, int count) {
        List<String> markets = coinListProvider.getMarketCodes();
        int totalSaved = 0;

        for (String market : markets) {
            int i = 0;
            while (i < count) {
                delay();
                try {
                    int size = Math.min(MAX_PER_REQUEST, count - i);
                    String url = String.format("https://api.upbit.com/v1/candles/%s?market=%s&count=%d",
                            interval.getSuffix(), market, size);

                    String json = rest.getForObject(url, String.class);
                    JsonNode array = mapper.readTree(json);

                    int saved = redisService.saveCandleArray(interval, market, array);
                    totalSaved += saved;

                    if (interval == CandleInterval.SEC && i == 0 && !array.isEmpty()) {
                        redisService.saveLatestCandle(market, array.get(0));
                    }

                    i += MAX_PER_REQUEST;

                } catch (Exception e) {
                    String message = e.getMessage() != null ? e.getMessage() : "unknown";
                    System.err.printf("❌ [%s:%s] 데이터 수집 실패: %s%n", interval, market, message);

                    if (message.contains("429") || message.toLowerCase().contains("too many request")) {
                        System.err.println("🕒 429 Too Many Requests - 1분간 대기 후 재시도");
                        try {
                            Thread.sleep(60_000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return totalSaved;
                        }
                    } else {
                        i += MAX_PER_REQUEST;
                    }
                }
            }
        }

        return totalSaved;
    }

    public int fetchUntil(CandleInterval interval, int requiredSize) {
        int total = 0;
        for (String market : coinListProvider.getMarketCodes()) {
            int current = redisService.countCandles(interval, market);
            if (current >= requiredSize) continue;

            int toFetch = requiredSize - current;
            int saved = fetchInterval(interval, toFetch);
            total += saved;
        }
        return total;
    }

    private void delay() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}