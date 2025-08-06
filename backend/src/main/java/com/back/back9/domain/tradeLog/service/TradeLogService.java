package com.back.back9.domain.tradeLog.service;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.tradeLog.dto.TradeLogDto;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.repository.TradeLogRepository;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class TradeLogService {
    private final TradeLogRepository tradeLogRepository;
    private final WalletRepository walletRepository;
    private final CoinRepository coinRepository;

    public TradeLogService(TradeLogRepository tradeLogRepository,
                           WalletRepository walletRepository,
                           CoinRepository coinRepository) {
        this.tradeLogRepository = tradeLogRepository;
        this.walletRepository = walletRepository;
        this.coinRepository = coinRepository;

    }
    @Transactional(readOnly = true)
    public List<TradeLog> findAll() {
        return tradeLogRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Optional<TradeLog> findLatest() {
        return tradeLogRepository.findFirstByOrderByIdDesc();

    }
    @Transactional(readOnly = true)
    public List<TradeLogDto> findByWalletId(Long walletId) {
        return tradeLogRepository.findByWalletId(walletId)
                .stream()
                .map(TradeLogDto::from)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<TradeLogDto> findByFilter(Long walletId, TradeType type, Integer coinId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<TradeLog> logs = tradeLogRepository.findByWalletIdFilter(walletId, type, coinId, startDate, endDate, pageable);

        return logs.stream()
                .map(TradeLogDto::from)
                .collect(Collectors.toList());
    }

    // 새로운 메서드: userId로 거래 내역 조회
    @Transactional(readOnly = true)
    public List<TradeLogDto> findByUserIdAndFilter(Long userId, TradeType type, Integer coinId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        // userId로 지갑 찾기
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 ID " + userId + "의 지갑을 찾을 수 없습니다."));

        // 지갑 ID를 Long 타입으로 직접 사용
        Page<TradeLog> logs = tradeLogRepository.findByWalletIdFilter(wallet.getId(), type, coinId, startDate, endDate, pageable);

        return logs.stream()
                .map(TradeLogDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeLogDto> findByWalletIdAndTypeCharge(Long walletId) {
        return findByWalletId(walletId).stream()
                .filter(log -> log.tradeType() == com.back.back9.domain.tradeLog.entity.TradeType.CHARGE)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public int count() {
        return (int) tradeLogRepository.count();
    }

    @Transactional
    public void saveAll(List<TradeLog> tradeLogs) {
        tradeLogRepository.saveAll(tradeLogs);
    }
    @Transactional
    public TradeLogDto save(TradeLogDto tradeLogDto) {
        Wallet wallet = walletRepository.findById((long) tradeLogDto.walletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        Coin coin = coinRepository.findById((long) tradeLogDto.coinId())
                .orElseThrow(() -> new EntityNotFoundException("Coin not found"));

        TradeLog tradeLog = TradeLogDto.toEntity(tradeLogDto, wallet, coin);
        TradeLog savedTradeLog = tradeLogRepository.save(tradeLog);

        return TradeLogDto.from(savedTradeLog);
    }

    @Transactional
    public TradeLog save(TradeLog tradeLog) {

        return tradeLogRepository.save(tradeLog);
    }
    @Transactional
    public void createMockLogs() {
        if (count() > 0) return;
        coinRepository.deleteAll();
        Wallet wallet = walletRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("wallet not found"));
        Coin coin1 = coinRepository.save(
                Coin.builder()
                        .koreanName("비트코인1")
                        .englishName("Bitcoin1")
                        .symbol("BTC1")
                        .build()
        );

        Coin coin2 = coinRepository.save(
                Coin.builder()
                        .koreanName("이더리움1")
                        .englishName("Ethereum1")
                        .symbol("ETH1")
                        .build()
        );

        Coin coin3 = coinRepository.save(
                Coin.builder()
                        .koreanName("리플1")
                        .englishName("Ripple1")
                        .symbol("XRP1")
                        .build()
        );
        List<TradeLog> logs = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.of(2025, 7, 25, 0, 0);

        for (int i = 1; i <= 15; i++) {
            Coin coin = (i <= 9) ? coin1 : coin2;
            TradeType type = (i % 3 == 0) ? TradeType.SELL : TradeType.BUY;

            TradeLog log = TradeLog.builder()
                    .wallet(wallet)
                    .coin(coin)
                    .type(type)
                    .quantity(BigDecimal.valueOf(1))
                    .price(BigDecimal.valueOf(100_000_000L + (i * 10_000_000L)))
                    .build();
            log.setCreatedAt(baseDate.plusDays((i - 1) * 7));
            logs.add(log);
        }

        saveAll(logs);

    }

}
