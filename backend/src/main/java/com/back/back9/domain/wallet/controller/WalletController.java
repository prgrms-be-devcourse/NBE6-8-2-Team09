package com.back.back9.domain.wallet.controller;

import com.back.back9.domain.wallet.dto.ChargePointsRequest;
import com.back.back9.domain.wallet.dto.WalletResponse;
import com.back.back9.domain.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Validated
@Slf4j
public class WalletController {

    private final WalletService walletService;

    // 사용자 지갑 정보 조회 (모든 코인 수량 포함)
    @GetMapping("/users/{userId}")
    public ResponseEntity<WalletResponse> getUserWallet(@PathVariable Long userId) {
        log.info("사용자 지갑 조회 요청 - 사용자 ID: {}", userId);
        return walletService.getUserWallet(userId);
    }

    // 지갑 잔액 충전
    @PostMapping("/users/{userId}/charge")
    public ResponseEntity<WalletResponse> chargeWallet(
            @PathVariable Long userId,
            @Valid @RequestBody ChargePointsRequest request) {

        log.info("지갑 충전 요청 - 사용자 ID: {}, 충전 금액: {}",
                userId, request.getAmount());

        return walletService.chargeWallet(userId, request);
    }
}
