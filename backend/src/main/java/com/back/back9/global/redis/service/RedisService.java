package com.back.back9.global.redis.service;

import com.back.back9.domain.websocket.vo.CandleInterval;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_EXISTING_TO_CHECK = 1000;

    public int saveCandleArray(CandleInterval interval, String symbol, JsonNode candleArray) {
        String redisKey = interval.redisKey(symbol);
        Map<String, JsonNode> existingMap = new HashMap<>();
        int inserted = 0;

        List<String> existing = redisTemplate.opsForList().range(redisKey, 0, MAX_EXISTING_TO_CHECK - 1);
        if (existing != null) {
            for (String json : existing) {
                try {
                    JsonNode parsed = mapper.readTree(json);
                    String time = parsed.path("candle_date_time_kst").asText();
                    existingMap.put(time, parsed);
                } catch (Exception ignored) {
                }
            }
        }

        for (JsonNode candle : candleArray) {
            String kstTime = candle.path("candle_date_time_kst").asText();
            ObjectNode modified = candle.deepCopy();
            modified.remove("candle_date_time_utc");

            long ts = candle.has("timestamp") ? candle.path("timestamp").asLong(0) : 0;
            if (ts == 0 && !kstTime.isEmpty()) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(kstTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    ts = ldt.toInstant(ZoneOffset.ofHours(9)).toEpochMilli();
                } catch (Exception e) {
                    log.warn("❗ timestamp 보정 실패: {}", kstTime);
                }
            }
            modified.put("timestamp", ts);

            // 병합 여부
            if (existingMap.containsKey(kstTime)) {
                JsonNode merged = mergeCandles(existingMap.get(kstTime), modified);
                redisTemplate.opsForList().remove(redisKey, 1, existingMap.get(kstTime).toString());
                redisTemplate.opsForList().leftPush(redisKey, merged.toString());
            } else {
                redisTemplate.opsForList().leftPush(redisKey, modified.toString());
            }

            inserted++;
        }

        redisTemplate.opsForList().trim(redisKey, 0, interval.getMaxSize() - 1);
        return inserted;
    }

    public void saveCandle(CandleInterval interval, String symbol, JsonNode candle) {
        String redisKey = interval.redisKey(symbol);
        String kstTime = candle.path("candle_date_time_kst").asText();

        List<String> existing = redisTemplate.opsForList().range(redisKey, 0, MAX_EXISTING_TO_CHECK - 1);
        ObjectNode modified = candle.deepCopy();
        long ts = candle.has("timestamp") ? candle.path("timestamp").asLong(0) : 0;

        if (ts == 0 && !kstTime.isEmpty()) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(kstTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                ts = ldt.toInstant(ZoneOffset.ofHours(9)).toEpochMilli();
            } catch (Exception e) {
                log.warn("❗ WebSocket timestamp 변환 실패: {}", kstTime);
            }
        }
        modified.put("timestamp", ts);

        if (existing != null) {
            for (String json : existing) {
                try {
                    JsonNode parsed = mapper.readTree(json);
                    String time = parsed.path("candle_date_time_kst").asText();
                    if (kstTime.equals(time)) {
                        JsonNode merged = mergeCandles(parsed, modified);
                        redisTemplate.opsForList().remove(redisKey, 1, json);
                        redisTemplate.opsForList().leftPush(redisKey, merged.toString());
                        redisTemplate.opsForList().trim(redisKey, 0, interval.getMaxSize() - 1);
                        log.debug("🔁 캔들 병합 완료 (symbol={}, KST={})", symbol, kstTime);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }

        redisTemplate.opsForList().leftPush(redisKey, modified.toString());
        redisTemplate.opsForList().trim(redisKey, 0, interval.getMaxSize() - 1);
        log.debug("✅ 캔들 저장 완료 (symbol={}, KST={}, timestamp={})", symbol, kstTime, ts);
    }

    public void saveLatestCandle(String symbol, JsonNode candleNode) {
        String key = symbol + ":Latest";
        redisTemplate.opsForValue().set(key, candleNode.toString());
    }

    private JsonNode mergeCandles(JsonNode oldCandle, JsonNode newCandle) {
        ObjectNode merged = oldCandle.deepCopy();

        // 시가(opening_price): 더 이른 시간의 값
        long oldTs = oldCandle.path("timestamp").asLong(0);
        long newTs = newCandle.path("timestamp").asLong(0);
        merged.put("opening_price", oldTs <= newTs ? oldCandle.path("opening_price").asDouble() : newCandle.path("opening_price").asDouble());

        // 종가(trade_price): 더 늦은 시간의 값
        merged.put("trade_price", oldTs >= newTs ? oldCandle.path("trade_price").asDouble() : newCandle.path("trade_price").asDouble());

        // 고가(high_price): 최대
        merged.put("high_price", Math.max(oldCandle.path("high_price").asDouble(), newCandle.path("high_price").asDouble()));

        // 저가(low_price): 최소
        merged.put("low_price", Math.min(oldCandle.path("low_price").asDouble(), newCandle.path("low_price").asDouble()));

        // 누적 거래량
        merged.put("candle_acc_trade_volume", oldCandle.path("candle_acc_trade_volume").asDouble(0) + newCandle.path("candle_acc_trade_volume").asDouble(0));

        // 누적 거래금액
        merged.put("candle_acc_trade_price", oldCandle.path("candle_acc_trade_price").asDouble(0) + newCandle.path("candle_acc_trade_price").asDouble(0));

        return merged;
    }

    public List<JsonNode> getLatestCandle(CandleInterval interval, String symbol) {
        String key = interval.redisKey(symbol);
        List<String> rawList = redisTemplate.opsForList().range(key, 0, 169);
        if (rawList == null) return List.of();

        return rawList.stream().map(this::parseJson).sorted(Comparator.comparing(node -> LocalDateTime.parse(node.path("candle_date_time_kst").asText()), Comparator.reverseOrder())).collect(Collectors.toList());
    }

    public List<JsonNode> getPreviousCandlesByRange(CandleInterval interval, String symbol, int page, LocalDateTime time) {
        final int PAGE_SIZE = 50;
        final int INITIAL_RENDER_SIZE = 120;
        final int OFFSET = INITIAL_RENDER_SIZE + (page * PAGE_SIZE);
        final int REDIS_FETCH_LIMIT = 1000;

        String key = interval.redisKey(symbol);
        List<String> rawList = redisTemplate.opsForList().range(key, 0, REDIS_FETCH_LIMIT - 1);
        if (rawList == null || rawList.isEmpty()) return List.of();

        List<JsonNode> parsedList = rawList.stream().map(this::parseJson).filter(node -> {
            String dateStr = node.path("candle_date_time_kst").asText();
            try {
                return LocalDateTime.parse(dateStr).isBefore(time);
            } catch (Exception e) {
                return false;
            }
        }).sorted(Comparator.comparing(node -> LocalDateTime.parse(node.path("candle_date_time_kst").asText()), Comparator.reverseOrder())).toList();

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
        Optional.ofNullable(redisTemplate.getConnectionFactory()).map(RedisConnectionFactory::getConnection).ifPresent(connection -> {
            try (connection) {
                connection.serverCommands().flushDb();
            }
        });
    }

    public void sortAndRewrite() {
        log.info("✅ Redis key 전체 정렬 시작");

        Set<String> keys = redisTemplate.keys("*");
        if (keys == null) return;

        for (String key : keys) {
            List<String> list = redisTemplate.opsForList().range(key, 0, -1);
            if (list == null || list.isEmpty()) continue;

            try {
                List<Map<String, Object>> parsedList = new ArrayList<>();
                for (String item : list) {
                    parsedList.add(mapper.readValue(item, Map.class));
                }

                Map<String, Map<String, Object>> deduped = new TreeMap<>();
                for (Map<String, Object> entry : parsedList) {
                    String time = (String) entry.get("candle_date_time_kst");
                    deduped.put(time, entry);
                }

                List<String> sorted = deduped.values().stream().sorted(Comparator.comparing(e -> (String) e.get("candle_date_time_kst"))).map(e -> {
                    try {
                        return mapper.writeValueAsString(e);
                    } catch (Exception ex) {
                        return null;
                    }
                }).filter(Objects::nonNull).toList();

                redisTemplate.delete(key);
                for (String json : sorted) {
                    redisTemplate.opsForList().rightPush(key, json);
                }

                log.info("✅ Redis key '{}' 정렬 완료 ({}개)", key, sorted.size());

            } catch (Exception e) {
                log.error("❌ Redis 정렬 중 오류 발생 (key={}): {}", key, e.getMessage());
            }
        }
    }
}