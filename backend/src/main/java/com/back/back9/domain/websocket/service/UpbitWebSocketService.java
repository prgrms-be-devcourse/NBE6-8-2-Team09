package com.back.back9.domain.websocket.service;

import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import com.back.back9.global.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpbitWebSocketService {

    private final RedisService redisService;
    private WebSocketClient client;

    // 구독 메시지 생성 함수
    private String buildSubscribeMessage() {
        return "[{\"ticket\":\"" + UUID.randomUUID() + "\"}," +
                "{\"type\":\"candle.1s\",\"codes\":[" +
                "\"KRW-BTC\",\"KRW-ETH\",\"KRW-XRP\",\"KRW-SOL\",\"KRW-ADA\"," +
                "\"KRW-DOGE\",\"KRW-MATIC\",\"KRW-LINK\",\"KRW-LTC\",\"KRW-TRX\"," +
                "\"KRW-AVAX\",\"KRW-AAVE\",\"KRW-STX\",\"KRW-FET\",\"KRW-SAND\"" +
                "]}," +
                "{\"format\":\"DEFAULT\"}]";
    }

    // 웹소켓 연결
    public void connect() {
        if (client != null && client.isOpen()) {
            System.out.println("이미 연결되어 있습니다.");
            return;
        }

        try {
            client = new WebSocketClient(new URI("wss://api.upbit.com/websocket/v1")) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("WebSocket 연결됨");
                    send(buildSubscribeMessage());
                    System.out.println("구독 메시지 전송됨");
                }

                // ✔ 필수 구현 (빈 메서드)
                @Override
                public void onMessage(String message) {
                    // 사용하지 않음
                }

                // ✔ 실제 수신 처리 (ByteBuffer)
                @Override
                public void onMessage(ByteBuffer bytes) {
                    String msg = StandardCharsets.UTF_8.decode(bytes).toString();


                    redisService.saveCandle(msg);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket 연결 종료됨: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    throw new ErrorException(ErrorCode.INTERNAL_ERROR, "WebSocket 에러 발생: " + ex.getMessage());
                }
            };

            client.connect(); // 연결 시도

        } catch (Exception e) {
            throw new ErrorException(ErrorCode.INTERNAL_ERROR, "WebSocket 연결 실패: " + e.getMessage());
        }
    }

    // 웹소켓 종료
    public void disconnect() {
        if (client != null && client.isOpen()) {
            client.close();
            System.out.println("WebSocket 수동 종료 요청");
        }
    }
}