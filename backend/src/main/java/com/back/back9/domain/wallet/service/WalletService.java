package com.back.back9.domain.wallet.service;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.repository.TradeLogRepository;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.wallet.dto.*;
import com.back.back9.domain.wallet.entity.CoinAmount;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.CoinAmountRepository;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final CoinAmountRepository coinAmountRepository;
    private final CoinRepository coinRepository; // 추가된 부분
    private final UserRepository userRepository; // 사용자 정보 조회를 위한 리포지토리
    private final TradeLogRepository tradeLogRepository;


    // 사용자 지갑 생성
    @Transactional
    public Wallet createWallet(Long userId) {
        // 이미 지갑이 존재하는지 확인
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new ErrorException(ErrorCode.WALLET_ALREADY_EXISTS, userId);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ErrorException(ErrorCode.USER_NOT_FOUND, userId));

        // 새 지갑 생성
        Wallet wallet = Wallet.builder()
                .user(user)
                .address("Wallet_address_" + userId)
                .balance(BigDecimal.valueOf(500000000))
                .build();

        walletRepository.save(wallet);

        log.info("새 지갑 생성 완료 - 사용자 ID: {}, 주소: {}", userId, wallet.getAddress());

        return wallet;
    }

    // 사용자 지갑 정보 조회 (모든 코인 수량 포함)
    @Transactional(readOnly = true)
    public ResponseEntity<WalletResponse> getUserWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, userId));


        List<CoinAmount> validCoinAmounts = wallet.getCoinAmounts()
                .stream()
                .filter(this::isValidCoinAmount)
                .toList();

        log.info("사용자 지갑 조회 완료 - 사용자 ID: {}, 전체 코인: {}개, 유효한 코인: {}개",
                userId, wallet.getCoinAmounts().size(), validCoinAmounts.size());

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

        // 거래 로그 저장 (충전)
        TradeLog chargeLog = TradeLog.builder()
                .wallet(wallet)
                .type(TradeType.CHARGE)
                .quantity(BigDecimal.ONE)
                .price(request.getAmount())
                .coin(null)
                .build();

        tradeLogRepository.save(chargeLog);

        log.info("지갑 잔액 충전 완료 - 사용자 ID: {}, 충전 금액: {}, 현재 잔액: {}",
                userId, request.getAmount(), wallet.getBalance());

        // coinAmounts는 항상 빈 리스트로 초기화되므로 null 체크 불필요
        List<CoinAmount> validCoinAmounts = wallet.getCoinAmounts()
                .stream()
                .filter(this::isValidCoinAmount)
                .toList();

        WalletResponse response = WalletResponse.fromWithValidCoinAmounts(wallet, validCoinAmounts);
        return ResponseEntity.ok(response);
    }

    // 지갑의 코인 보유 정보를 조회하여 평가 수익률 계산에 사용
    public List<CoinHoldingInfo> getCoinHoldingsForProfitCalculation(Long walletId) {
        // 지갑 ID로 지갑 조회
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, walletId));

        // coinAmounts는 항상 빈 리스트로 초기화되므로 null 체크 불필요
        List<CoinAmount> validCoinAmounts = wallet.getCoinAmounts()
                .stream()
                .filter(this::isValidCoinAmount)
                .toList();

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

    // 범용 거래 처리 메서드 (구매/판매)
    @Transactional
    public ResponseEntity<WalletResponse> processTransaction(Long userId, BuyCoinRequest request, TransactionType transactionType) {
        // ID로 코인과 지갑 조회
        Coin coin = coinRepository.findById(request.coinId())
                .orElseThrow(() -> new ErrorException(ErrorCode.COIN_NOT_FOUND, request.coinId()));

        Wallet wallet = walletRepository.findById(request.walletId())
                .orElseThrow(() -> new ErrorException(ErrorCode.WALLET_NOT_FOUND, request.walletId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException(ErrorCode.USER_NOT_FOUND, userId));




        // 지갑의 소유자가 요청한 사용자와 일치하는지 확인
        if (!wallet.getUser().getId().equals(userId)) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED, "지갑에 대한 접근 권한이 없습니다.");
        }

        // 거래 금액 유효성 검사
        if(request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ErrorException(ErrorCode.INVALID_REQUEST, "거래 금액은 0보다 커야 합니다.");
        }

        // 거래 타입에 따른 지갑 잔액 처리
        if (transactionType == TransactionType.BUY) {
            // 구매: 잔액 부족 검사 후 차감
            if(wallet.getBalance().compareTo(request.amount()) < 0) {
                throw new ErrorException(ErrorCode.INSUFFICIENT_BALANCE, "잔액이 부족합니다.");
            }
            wallet.deduct(request.amount());
        } else if (transactionType == TransactionType.SELL) {
            // 판매: 지갑 잔액 증가
            wallet.charge(request.amount());
        }

        // 지갑 정보 저장
        walletRepository.save(wallet);

        log.info("{} 완료 - 사용자 ID: {}, 코인 ID: {}, 거래 금액: {}, 현재 잔액: {}",
                transactionType == TransactionType.BUY ? "구매" : "판매",
                userId, coin.getId(), request.amount(), wallet.getBalance());

        // coinAmounts는 항상 빈 리스트로 초기화되므로 null 체크 불필요
        List<CoinAmount> validCoinAmounts = wallet.getCoinAmounts()
                .stream()
                .filter(this::isValidCoinAmount)
                .filter(ca -> ca.getCoin().getId().equals(coin.getId()))
                .toList();

        // 해당 코인의 CoinAmount가 없다면 빈 CoinAmount 생성 (구매 시에만)
        if (validCoinAmounts.isEmpty() && transactionType == TransactionType.BUY) {
            log.info("사용자 ID {}의 지갑에 코인 ID {}가 없습니다. 새로운 CoinAmount 생성",
                    userId, coin.getId());
            CoinAmount newCoinAmount = CoinAmount.builder()
                    .coin(coin)
                    .wallet(wallet)
                    .quantity(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
            coinAmountRepository.save(newCoinAmount);

            // wallet의 coinAmounts 리스트에 새로 생성한 CoinAmount 추가
            wallet.getCoinAmounts().add(newCoinAmount);
            validCoinAmounts = List.of(newCoinAmount);
        } else if (validCoinAmounts.isEmpty() && transactionType == TransactionType.SELL) {
            // 판매 시 해당 코인이 없으면 에러
            throw new ErrorException(ErrorCode.INSUFFICIENT_BALANCE, "판매할 코인이 없습니다.");
        }

        // 거래 타입에 따른 CoinAmount 업데이트
        CoinAmount targetCoinAmount = validCoinAmounts.get(0);

        if (transactionType == TransactionType.BUY) {
            targetCoinAmount.addQuantityAndAmount(request.quantity(), request.amount());
        } else if (transactionType == TransactionType.SELL) {
            // 판매 시 보유 수량 검사
            if (targetCoinAmount.getQuantity().compareTo(request.quantity()) < 0) {
                throw new ErrorException(ErrorCode.INSUFFICIENT_BALANCE, "보유 수량이 부족합니다.");
            }
            targetCoinAmount.subtractQuantityAndAmount(request.quantity(), request.amount());
        }

        // CoinAmount 저장 및 wallet의 coinAmounts 리스트 동기화
        coinAmountRepository.save(targetCoinAmount);

        // wallet의 기존 리스트에서 해당 CoinAmount를 찾아서 업데이트
        List<CoinAmount> walletCoinAmounts = wallet.getCoinAmounts();
        for (int i = 0; i < walletCoinAmounts.size(); i++) {
            if (walletCoinAmounts.get(i).getId().equals(targetCoinAmount.getId())) {
                walletCoinAmounts.set(i, targetCoinAmount);
                break;
            }
        }

        WalletResponse response = WalletResponse.fromWithValidCoinAmounts(wallet,
                wallet.getCoinAmounts().stream().filter(this::isValidCoinAmount).toList());
        return ResponseEntity.ok(response);
    }

    // 구매 편의 메서드
    public ResponseEntity<WalletResponse> purchaseItem(Long userId, BuyCoinRequest request) {
        return processTransaction(userId, request, TransactionType.BUY);
    }

    // 판매 편의 메서드
    public ResponseEntity<WalletResponse> sellItem(Long userId, BuyCoinRequest request) {
        return processTransaction(userId, request, TransactionType.SELL);
    }

    public void deleteWalletByUserId(Long userId) {
        walletRepository.findByUserId(userId)
                .ifPresent(walletRepository::delete);
    }
    public boolean existsByUserId(Long userId) {
        return walletRepository.existsByUserId(userId);
    }
}