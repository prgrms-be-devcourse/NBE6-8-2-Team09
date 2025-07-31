package com.back.back9.domain.wallet.service;

import com.back.back9.domain.wallet.dto.ChargePointsRequest;
import com.back.back9.domain.wallet.dto.CoinHoldingInfo;
import com.back.back9.domain.wallet.dto.WalletResponse;
import com.back.back9.domain.wallet.entity.CoinAmount;
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
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;

    // 사용자 지갑 정보 조회 (모든 코인 수량 포함)
    @Transactional(readOnly = true)
    public ResponseEntity<WalletResponse> getUserWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, userId));

        // coinAmounts가 null일 수 있으므로 안전한 처리
        List<CoinAmount> coinAmounts = wallet.getCoinAmounts();
        List<CoinAmount> validCoinAmounts = coinAmounts != null ?
                coinAmounts.stream().filter(this::isValidCoinAmount).toList() :
                Collections.emptyList();

        log.info("사용자 지갑 조회 완료 - 사용자 ID: {}, 전체 코인: {}개, 유효한 코인: {}개",
                userId, coinAmounts != null ? coinAmounts.size() : 0, validCoinAmounts.size());

        WalletResponse response = WalletResponse.fromWithValidCoinAmounts(wallet, validCoinAmounts);
        return ResponseEntity.ok(response);
    }

    // 지갑 잔액 충전
    @Transactional
    public ResponseEntity<WalletResponse> chargeWallet(Long userId, ChargePointsRequest request) {
        // 지갑 조회
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, userId));

        // 충전 금액 유효성 검사
        if(request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ErrorException(ErrorCode.INVALID_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }

        // 지갑 잔액에 충전
        wallet.charge(request.getAmount());

        // 지갑 정보 저장
        walletRepository.save(wallet);

        log.info("지갑 잔액 충전 완료 - 사용자 ID: {}, 충전 금액: {}, 현재 잔액: {}",
                userId, request.getAmount(), wallet.getBalance());

        // coinAmounts가 null일 수 있으므로 안전한 처리
        List<CoinAmount> coinAmounts = wallet.getCoinAmounts();
        List<CoinAmount> validCoinAmounts = coinAmounts != null ?
                coinAmounts.stream().filter(this::isValidCoinAmount).toList() :
                Collections.emptyList();

        WalletResponse response = WalletResponse.fromWithValidCoinAmounts(wallet, validCoinAmounts);
        return ResponseEntity.ok(response);
    }

    // 지갑의 코인 보유 정보를 조회하여 평가 수익률 계산에 사용
    public List<CoinHoldingInfo> getCoinHoldingsForProfitCalculation(Long walletId) {
        // 지갑 ID로 지갑 조회
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, walletId));

        // coinAmounts가 null일 수 있으므로 안전한 처리
        List<CoinAmount> coinAmounts = wallet.getCoinAmounts();
        List<CoinAmount> validCoinAmounts = coinAmounts != null ?
                coinAmounts.stream().filter(this::isValidCoinAmount).toList() :
                Collections.emptyList();

        // CoinAmount를 CoinHoldingInfo로 변환
        List<CoinHoldingInfo> coinHoldings = validCoinAmounts.stream()
                .map(CoinHoldingInfo::from)  // 시세 정보는 Exchange에서 별도 제공
                .toList();

        log.info("지갑 ID {}의 코인 보유 정보 조회 완료 - 보유 코인 종류: {}개",
                walletId, coinHoldings.size());

        return coinHoldings;
    }

    // 사용자 ID로 지갑의 코인 보유 정보를 조회하여 평가 수익률 계산에 사용
    public List<CoinHoldingInfo> getCoinHoldingsByUserId(Long userId) {
        // 사용자 ID로 지갑 조회
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, userId));

        return getCoinHoldingsForProfitCalculation(wallet.getId());
    }

    // CoinAmount 유효성 검사
    private boolean isValidCoinAmount(CoinAmount coinAmount) {
        if (coinAmount == null) {
            log.warn("CoinAmount가 null입니다.");
            throw new ErrorException(ErrorCode.INVALID_COIN_DATA, "null");
        }

        if (coinAmount.getCoin() == null) {
            log.warn("CoinAmount ID {}의 Coin 정보가 null입니다.", coinAmount.getId());
            throw new ErrorException(ErrorCode.INVALID_COIN_DATA, coinAmount.getId());
        }

        if (coinAmount.getCoin().getId() <= 0) {
            log.warn("유효하지 않은 코인 ID: {}", coinAmount.getCoin().getId());
            throw new ErrorException(ErrorCode.INVALID_COIN_DATA, coinAmount.getId());
        }

        if (coinAmount.getCoin().getSymbol() == null || coinAmount.getCoin().getSymbol().trim().isEmpty()) {
            log.warn("코인 ID {}의 심볼이 비어있습니다.", coinAmount.getCoin().getId());
            throw new ErrorException(ErrorCode.INVALID_COIN_DATA, coinAmount.getId());
        }

        if (coinAmount.getTotalAmount() == null) {
            log.warn("CoinAmount ID {}의 수량 정보가 null입니다.", coinAmount.getId());
            throw new ErrorException(ErrorCode.INVALID_COIN_DATA, coinAmount.getId());
        }

        if (coinAmount.getQuantity() == null) {
            log.warn("CoinAmount ID {}의 코인 개수 정보가 null입니다.", coinAmount.getId());
            throw new ErrorException(ErrorCode.INVALID_COIN_DATA, coinAmount.getId());
        }

        if (coinAmount.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("CoinAmount ID {}의 총 금액이 음수입니다: {}", coinAmount.getId(), coinAmount.getTotalAmount());
            return false;
        }

        if (coinAmount.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("CoinAmount ID {}의 코인 개수가 음수입니다: {}", coinAmount.getId(), coinAmount.getQuantity());
            throw new ErrorException(ErrorCode.INVALID_COIN_DATA, coinAmount.getId());
        }

        return true;
    }
}
