package com.back.back9.domain.websocket.service;

import com.back.back9.domain.websocket.mock.MockCoinListProvider;
import com.back.back9.domain.websocket.vo.CandleInterval;
import com.back.back9.global.redis.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class UpbitRestCandleFetcher {

    private final RedisService redisService;
    private final MockCoinListProvider coinListProvider;
    private final RestTemplate rest = new RestTemplate();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_PER_REQUEST = 200;

    public void fetchInterval(CandleInterval interval, int totalCount) {
        for (String market : coinListProvider.getMarketCodes()) {
            try {
                for (int i = 0; i < totalCount; i += MAX_PER_REQUEST) {
                    delaySafely();
                    String url = String.format(
                            "https://api.upbit.com/v1/candles/%s?market=%s&count=%d",
                            interval.getSuffix(), market, Math.min(MAX_PER_REQUEST, totalCount - i));
                    JsonNode arr = fetchJsonArray(url);
                    redisService.saveCandleArray(interval, market, arr);
                    if (interval == CandleInterval.SEC && i == 0 && !arr.isEmpty()) {
                        redisService.saveLatestCandle(market, arr.get(0));
                    }
                }
            } catch (Exception e) {
                System.err.printf("REST fetch 실패 [%s:%s]: %s%n", interval, market, e.getMessage());
            }
        }
    }

    private JsonNode fetchJsonArray(String url) throws Exception {
        String res = rest.getForObject(url, String.class);
        return mapper.readTree(res);
    }

    private void delaySafely() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}