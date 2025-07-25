package com.back.back9.domain.websocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpbitWebSocketConnectService {

    private final UpbitWebSocketService upbitWebSocketService;

    public void startSocket() {
        upbitWebSocketService.connect();
    }

    public void stopSocket() {
        upbitWebSocketService.disconnect();
    }
}