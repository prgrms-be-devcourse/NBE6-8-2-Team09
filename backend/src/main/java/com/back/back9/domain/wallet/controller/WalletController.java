package com.back.back9.domain.wallet.controller;

import com.back.back9.domain.wallet.dto.ChargePointsRequest;
import com.back.back9.domain.wallet.dto.WalletBalanceResponse;
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

    // 지갑 잔액 조회(GET 요청)
    @GetMapping("/users/{userId}/coins/{coinId}")
    public ResponseEntity<WalletBalanceResponse> getWalletBalance(
            @PathVariable int userId,
            @PathVariable int coinId) {

        log.info("지갑 잔액 조회 요청 - 사용자 ID: {}, 코인 ID: {}", userId, coinId);
        return walletService.getWalletBalance(userId, coinId);
    }

    // 포인트 충전(POST 요청)
    @PostMapping("/users/{userId}/coins/{coinId}/charge")
    public ResponseEntity<WalletBalanceResponse> chargePoints(
            @PathVariable int userId,
            @PathVariable int coinId,
            @Valid @RequestBody ChargePointsRequest request) {

        log.info("포인트 충전 요청 - 사용자 ID: {}, 코인 ID: {}, 충전 금액: {}",
                userId, coinId, request.getAmount());

        return walletService.chargePoints(userId, coinId, request);
    }


}
