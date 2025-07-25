package com.back.back9.global.redis.service;

import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import com.back.back9.global.redis.dto.RedisDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void initRedis() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands();
    }
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }


    public void saveCandle(String rawJson) {
        try {
            JsonNode node = objectMapper.readTree(rawJson);

            String type = node.get("type").asText();
            if (!"candle.1s".equals(type)) return;

            String symbol = node.get("code").asText();
            String kstTime = node.get("candle_date_time_kst").asText();

            BigDecimal open = new BigDecimal(node.get("opening_price").asText());
            BigDecimal high = new BigDecimal(node.get("high_price").asText());
            BigDecimal low = new BigDecimal(node.get("low_price").asText());
            BigDecimal close = new BigDecimal(node.get("trade_price").asText());
            BigDecimal volume = new BigDecimal(node.get("candle_acc_trade_volume").asText());
            long timestamp = node.get("timestamp").asLong();

            String redisKeyWithTime = symbol + ":" + kstTime;
            String latestKey = "latest:" + symbol;

            // 💡 toPlainString()으로 변환 후 저장
            RedisDTO dto = new RedisDTO(
                    open.toPlainString(),
                    high.toPlainString(),
                    low.toPlainString(),
                    close.toPlainString(),
                    volume.toPlainString(),
                    timestamp
            );

            String value = objectMapper.writeValueAsString(dto);

            redisTemplate.opsForValue().set(redisKeyWithTime, value);
            redisTemplate.opsForValue().set(latestKey, value);

        } catch (Exception e) {
            throw new ErrorException(ErrorCode.INTERNAL_ERROR, "Redis 저장 실패: " + e.getMessage());
        }
    }
}