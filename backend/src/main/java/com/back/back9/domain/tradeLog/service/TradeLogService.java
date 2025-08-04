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
    public List<TradeLogDto> findByWalletId(int walletId) {
        return tradeLogRepository.findByWalletId(walletId)
                .stream()
                .map(TradeLogDto::from)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<TradeLogDto> findByFilter(int walletId, TradeType type, Integer coinId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return tradeLogRepository.findByWalletId(walletId)
                .stream()
                .map(TradeLogDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeLogDto> findByWalletIdAndTypeCharge(int walletId) {
        return findByWalletId(walletId).stream()
                .filter(log -> log.tradeType() == com.back.back9.domain.tradeLog.entity.TradeType.CHARGE)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public int count() {
        return (int) tradeLogRepository.count();
    }
    @Transactional(readOnly = true)
    public void saveAll(List<TradeLog> tradeLogs) {
        tradeLogRepository.saveAll(tradeLogs);
    }
    @Transactional(readOnly = true)
    public TradeLogDto save(TradeLogDto tradeLogDto) {
        Wallet wallet = walletRepository.findById((long) tradeLogDto.walletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        Coin coin = coinRepository.findById((long) tradeLogDto.coinId())
                .orElseThrow(() -> new EntityNotFoundException("Coin not found"));

        TradeLog tradeLog = TradeLogDto.toEntity(tradeLogDto, wallet, coin);
        TradeLog savedTradeLog = tradeLogRepository.save(tradeLog);

        return TradeLogDto.from(savedTradeLog);
    }
    @Transactional(readOnly = true)
    public TradeLog save(TradeLog tradeLog) {
        return tradeLogRepository.save(tradeLog);
    }
    @Transactional(readOnly = true)
    public void createMockLogs() {
        if (count() > 0) return;

        Wallet wallet = walletRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("wallet not found"));
        Coin coin1 = coinRepository.save(
                Coin.builder()
                        .koreanName("비트코인")
                        .englishName("Bitcoin")
                        .symbol("BTC")
                        .build()
        );

        Coin coin2 = coinRepository.save(
                Coin.builder()
                        .koreanName("이더리움")
                        .englishName("Ethereum")
                        .symbol("ETH")
                        .build()
        );

        Coin coin3 = coinRepository.save(
                Coin.builder()
                        .koreanName("리플")
                        .englishName("Ripple")
                        .symbol("XRP")
                        .build()
        );
        List<TradeLog> logs = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.of(2025, 7, 25, 0, 0);

        for (int i = 1; i <= 15; i++) {
            TradeLog log = new TradeLog();
            log.setWallet(wallet);

//            if (i <= 5) log.setCoin(coin1);
//            else if (i <= 10) log.setCoin(coin2);
//            else log.setCoin(coin3);
            if (i <= 9) log.setCoin(coin1);
            else log.setCoin(coin2);
            TradeType type = (i % 3 == 0) ? TradeType.SELL : TradeType.BUY;
            log.setType(type);
            log.setCreatedAt(baseDate.plusDays((i - 1) * 7));
            log.setQuantity(BigDecimal.valueOf(1));
            log.setPrice(BigDecimal.valueOf(100_000_000L + (i * 10_000_000L)));

            logs.add(log);
        }

        saveAll(logs);
    }

}
