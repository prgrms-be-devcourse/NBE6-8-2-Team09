package com.back.back9.domain.websocket.controller;

import com.back.back9.domain.websocket.service.UpbitWebSocketConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ws")
public class WebSocketDebugController {

    private final UpbitWebSocketConnectService starterService;
    private final UpbitWebSocketConnectService stopperService;

    @GetMapping("/start")
    public ResponseEntity<String> startWebSocket() {
        starterService.startSocket();
        return ResponseEntity.ok("WebSocket 연결 시작");
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stopWebsocket(){
        stopperService.stopSocket();
        return ResponseEntity.ok("WebSocket 연결 중단");
    }

}