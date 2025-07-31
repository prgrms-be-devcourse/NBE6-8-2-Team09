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

import java.time.LocalDateTime;
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

    public List<TradeLog> findAll() {
        return tradeLogRepository.findAll();
    }
    public Optional<TradeLog> findLatest() {
        return tradeLogRepository.findFirstByOrderByIdDesc();

    }
    public List<TradeLogDto> findByWalletId(int walletId) {
        return tradeLogRepository.findByWalletId(walletId)
                .stream()
                .map(TradeLogDto::from)
                .collect(Collectors.toList());
    }
    public List<TradeLog> findByFilter(int walletId, TradeType type, Integer coinId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (type == null && coinId == null && startDate == null && endDate == null) {
            return tradeLogRepository.findByWalletId(walletId, pageable).getContent();
        }

        return tradeLogRepository.findByWalletIdFilter(walletId, type, coinId, startDate, endDate, pageable).getContent();
    }

    public List<TradeLogDto> findByWalletIdAndTypeCharge(int walletId) {
        return findByWalletId(walletId).stream()
                .filter(log -> log.tradeType() == com.back.back9.domain.tradeLog.entity.TradeType.CHARGE)
                .collect(Collectors.toList());
    }

    public int count() {
        return (int) tradeLogRepository.count();
    }

    public void saveAll(List<TradeLog> tradeLogs) {
        tradeLogRepository.saveAll(tradeLogs);
    }
    public TradeLogDto save(TradeLogDto tradeLogDto) {
        Wallet wallet = walletRepository.findById((long) tradeLogDto.walletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        Coin coin = coinRepository.findById((long) tradeLogDto.coinId())
                .orElseThrow(() -> new EntityNotFoundException("Coin not found"));

        TradeLog tradeLog = TradeLogDto.toEntity(tradeLogDto, wallet, coin);
        TradeLog savedTradeLog = tradeLogRepository.save(tradeLog);

        return TradeLogDto.from(savedTradeLog);
    }
    public TradeLog save(TradeLog tradeLog) {
        return tradeLogRepository.save(tradeLog);
    }

}
