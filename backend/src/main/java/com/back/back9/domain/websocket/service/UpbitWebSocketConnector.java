package com.back.back9.domain.websocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpbitWebSocketConnector implements DisposableBean {

    private final CandleWebSocketHandler candleWebSocketHandler;

    public void connect() {
        WebSocketClient client = new StandardWebSocketClient();

        String url = "wss://api.upbit.com/websocket/v1";
        WebSocketConnectionManager manager = new WebSocketConnectionManager(client, candleWebSocketHandler, url);
        manager.setAutoStartup(true);
        manager.start();

        log.info("Upbit WebSocket 연결 시도 완료");
    }

    @Override
    public void destroy() {
        log.info("Upbit WebSocket 연결 종료 처리");
    }
}