package com.back.back9.domain.websocket.service;

import com.back.back9.domain.websocket.mock.MockCoinListProvider;
import com.back.back9.domain.websocket.vo.CandleInterval;
import com.back.back9.global.redis.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
@Slf4j
public class CandleWebSocketHandler extends TextWebSocketHandler {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final MockCoinListProvider coinListProvider;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        try {
            List<String> codes = coinListProvider.getMarketCodes();

            String payload = objectMapper.writeValueAsString(List.of(
                    Map.of("ticket", "candle-subscription"),
                    Map.of("type", "candle.1s", "codes", codes),
                    Map.of("type", "candle.1m", "codes", codes),
                    Map.of("format", "SIMPLE")
            ));

            session.sendMessage(new TextMessage(payload));
            log.info("✅ Upbit WebSocket 구독 요청 전송 완료. 구독 코인 수: {}", codes.size());

        } catch (Exception e) {
            log.error("❌ WebSocket 구독 요청 전송 실패", e);
        }
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            parseAndSaveCandle(message.getPayload());
        } catch (IOException e) {
            log.error("❌ WebSocket 메시지 파싱 실패", e);
        }
    }

    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) {
        String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);
        try {
            parseAndSaveCandle(payload);
        } catch (IOException e) {
            log.error("❌ WebSocket 바이너리 메시지 파싱 실패", e);
        }
    }

    private synchronized void parseAndSaveCandle(String payload) throws IOException {
        JsonNode root = objectMapper.readTree(payload);
        String type = root.path("ty").asText();
        if (!type.startsWith("candle.")) return;

        String market = root.path("cd").asText();
        CandleInterval interval = CandleInterval.fromWebSocketType(type);

        // Redis 저장
        ObjectNode candleNode = objectMapper.createObjectNode();
        candleNode.put("market", market);
        candleNode.put("candle_date_time_kst", root.path("cdttmk").asText(""));
        candleNode.put("opening_price", root.path("op").asDouble(0));
        candleNode.put("high_price", root.path("hp").asDouble(0));
        candleNode.put("low_price", root.path("lp").asDouble(0));
        candleNode.put("trade_price", root.path("tp").asDouble(0));
        candleNode.put("timestamp", root.path("tms").asLong(0));
        candleNode.put("candle_acc_trade_price", root.path("catp").asDouble(0));
        candleNode.put("candle_acc_trade_volume", root.path("catv").asDouble(0));

        redisService.saveCandle(interval, market, candleNode);
        redisService.saveLatestCandle(market, candleNode);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
        log.error("❌ WebSocket 전송 오류", exception);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) {
        log.warn("🔌 WebSocket 연결 종료됨: {}", closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }
}