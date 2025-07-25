package com.back.back9.domain.wallet.service;

import com.back.back9.domain.wallet.dto.ChargePointsRequest;
import com.back.back9.domain.wallet.dto.WalletBalanceResponse;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor

@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;

    // 지갑 잔액 조회
    @Transactional(readOnly = true)
    public ResponseEntity<WalletBalanceResponse> getWalletBalance(int userId, int coinId) {

        // 저장소에서 사용자 ID와 코인 ID로 지갑 조회
        // 에러 발생 시 ErrorException(ErrorCode.WALLET_NOT_FOUND) 예외 발생
        Wallet wallet = walletRepository.findByUserIdAndCoinId(userId, coinId)
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, userId, coinId));

        // 지갑 잔액 조회 성공 시 WalletBalanceResponse 객체 생성
        WalletBalanceResponse response = WalletBalanceResponse.from(wallet);

        // 로그 기록
        log.info("지갑 잔액 조회 완료 - 사용자 ID: {}, 코인 ID: {}, 잔액: {}",
                userId, coinId, wallet.getBalance());
        return ResponseEntity.ok(response);
    }

    // 포인트 충전
    @Transactional
    public ResponseEntity<WalletBalanceResponse> chargePoints(int userId, int coinId, ChargePointsRequest request) {

        // 사용자 ID와 코인 ID로 지갑 조회
        // 에러 발생 시 ErrorException(ErrorCode.WALLET_NOT_FOUND) 예외 발생
        Wallet wallet = walletRepository.findByUserIdAndCoinId(userId, coinId)
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, userId, coinId));

        // 충전 금액이 0 이하인 경우 에러 발생
        if(request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ErrorException(ErrorCode.INVALID_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }

        // 충전 금액을 지갑 잔액에 추가
        wallet.charge(request.getAmount());


        // 지갑 정보를 저장
        walletRepository.save(wallet);

        // 충전 완료 후 로그 기록
        log.info("포인트 충전 완료 - 사용자 ID: {}, 충전 금액: {}, 잔액: {}",
                userId, request.getAmount(), wallet.getBalance());


        // WalletBalanceResponse 객체 생성 후 반환
        WalletBalanceResponse response = WalletBalanceResponse.from(wallet);
        return ResponseEntity.ok(response);
    }


}
