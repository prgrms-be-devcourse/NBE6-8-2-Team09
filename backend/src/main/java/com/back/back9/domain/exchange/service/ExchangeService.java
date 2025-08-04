package com.back.back9.domain.exchange.service;

import com.back.back9.domain.exchange.dto.CoinPriceResponse;
import com.back.back9.domain.exchange.dto.ExchangeDTO;
import com.back.back9.domain.websocket.vo.CandleInterval;
import com.back.back9.global.redis.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public List<ExchangeDTO> getInitialCandles(CandleInterval interval, String symbol) {
        List<JsonNode> jsonList = redisService.getLatestCandle(interval, symbol);
        return jsonList.stream()
                .map(json -> objectMapper.convertValue(json, ExchangeDTO.class))
                .collect(Collectors.toList());
    }

    public List<ExchangeDTO> getPreviousCandles(CandleInterval interval, String market, int page, LocalDateTime time) {
        List<JsonNode> jsonList = redisService.getPreviousCandlesByRange(interval, market, page, time);
        return jsonList.stream()
                .map(json -> objectMapper.convertValue(json, ExchangeDTO.class))
                .collect(Collectors.toList());
    }

    public CoinPriceResponse getLatestCandleByScan(String coinName) {
        try {
            String coinSymbol = CoinSymbolMapper.toMarketCode(coinName);

            JsonNode json = redisService.getLatest1sCandle(coinSymbol);

            if (json == null) {
                return new CoinPriceResponse(coinSymbol, BigDecimal.ZERO, LocalDateTime.now());
            }

            BigDecimal close = json.get("trade_price").decimalValue();
            LocalDateTime now = LocalDateTime.now().withNano(0);

            return new CoinPriceResponse(coinSymbol, close, now);
        } catch (Exception e) {
            throw new RuntimeException("getLatestCandleByScan 오류: " + e.getMessage(), e);
        }
    }
}