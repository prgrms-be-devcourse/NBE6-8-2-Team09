package com.back.back9.global.redis.service;

import com.back.back9.domain.websocket.vo.CandleInterval;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    public void saveCandleArray(CandleInterval interval, String symbol, JsonNode candleArray) {
        String redisKey = interval.redisKey(symbol);
        for (JsonNode candle : candleArray) {
            ObjectNode modified = candle.deepCopy();
            long rawTimestamp = candle.get("timestamp").asLong();

            modified.remove("candle_date_time_utc");
            modified.remove("candle_date_time_kst");

            LocalDateTime dateTime = LocalDateTime.ofEpochSecond(rawTimestamp / 1000, 0, ZoneOffset.UTC);
            modified.put("timestamp", dateTime.toString());

            redisTemplate.opsForList().leftPush(redisKey, modified.toString());
        }
        redisTemplate.opsForList().trim(redisKey, 0, interval.getMaxSize() - 1);
    }

    public void saveCandle(CandleInterval interval, String symbol, JsonNode json) {
        String key = interval.redisKey(symbol);
        redisTemplate.opsForList().leftPush(key, json.toString());
        redisTemplate.opsForList().trim(key, 0, interval.getMaxSize() - 1);
    }

    public void saveLatestCandle(String symbol, JsonNode candleNode) {
        String key = symbol + ":Latest";
        redisTemplate.opsForValue().set(key, candleNode.toString());
    }

    public List<JsonNode> getLatestCandle(CandleInterval interval, String symbol) {
        String key = interval.redisKey(symbol);
        List<String> rawList = redisTemplate.opsForList().range(key, 0, 119);
        if (rawList == null) return List.of();
        return rawList.stream().map(this::parseJson).collect(Collectors.toList());
    }

    public List<JsonNode> getPreviousCandlesByRange(CandleInterval interval, String symbol, int page, LocalDateTime time) {
        final int PAGE_SIZE = 50;
        final int INITIAL_RENDER_SIZE = 120;
        final int OFFSET = INITIAL_RENDER_SIZE + (page * PAGE_SIZE);
        final int REDIS_FETCH_LIMIT = 1000;

        String key = interval.redisKey(symbol);
        List<String> rawList = redisTemplate.opsForList().range(key, 0, REDIS_FETCH_LIMIT - 1);
        if (rawList == null || rawList.isEmpty()) return List.of();

        List<JsonNode> parsedList = rawList.stream()
                .map(this::parseJson)
                .filter(node -> node.path("timestamp").asText().compareTo(time.toString()) < 0)
                .toList();

        int fromIndex = Math.min(OFFSET, parsedList.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, parsedList.size());

        return parsedList.subList(fromIndex, toIndex);
    }

    public JsonNode getLatest1sCandle(String symbol) {
        String key = symbol + ":Latest";
        String value = redisTemplate.opsForValue().get(key);
        return (value == null) ? null : parseJson(value);
    }

    private JsonNode parseJson(String str) {
        try {
            return mapper.readTree(str);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    public int countCandles(CandleInterval interval, String symbol) {
        String key = interval.redisKey(symbol);
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size.intValue() : 0;
    }

    public void clearAll() {
        Optional.ofNullable(redisTemplate.getConnectionFactory())
                .map(RedisConnectionFactory::getConnection)
                .ifPresent(connection -> {
                    try (connection) {
                        connection.serverCommands().flushDb();
                    }
                });
    }
}