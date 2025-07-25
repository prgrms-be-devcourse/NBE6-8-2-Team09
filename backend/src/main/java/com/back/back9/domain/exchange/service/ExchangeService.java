package com.back.back9.domain.exchange.service;

import com.back.back9.domain.exchange.dto.CoinPriceResponse;
import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import com.back.back9.global.redis.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final RedisService redisService;
    private final StringRedisTemplate redisTemplate;

    public CoinPriceResponse getLatestCandleByScan(String symbol) {
        String marketCode = CoinSymbolMapper.toMarketCode(symbol);
        if (marketCode == null) return null;

        try {
            // Redis에서 "latest:<symbol>" 키로 조회
            String latestKey = "latest:" + marketCode;
            String json = redisService.getData(latestKey);

            if (json == null) return null;

            // JSON 파싱 후 close 값 추출
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            String close = String.valueOf(root.get("close"));

            // 시간 정보는 없음 → 현재 시간 사용 가능
            String now = LocalDateTime.now().toString();
            return new CoinPriceResponse(marketCode, now, close);
        } catch (Exception e) {
            throw new ErrorException(ErrorCode.INTERNAL_ERROR, "Redis 조회 오류: " + e.getMessage());
        }
    }
}
