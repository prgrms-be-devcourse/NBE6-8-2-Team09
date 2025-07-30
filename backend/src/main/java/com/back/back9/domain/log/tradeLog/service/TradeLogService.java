package com.back.back9.domain.log.tradeLog.service;

import com.back.back9.domain.log.tradeLog.dto.TradeLogDto;
import com.back.back9.domain.log.tradeLog.entity.TradeLog;
import com.back.back9.domain.log.tradeLog.entity.TradeType;
import com.back.back9.domain.log.tradeLog.repository.TradeLogRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TradeLogService {
    private final TradeLogRepository tradeLogRepository;

    public TradeLogService(TradeLogRepository tradeLogRepository) {
        this.tradeLogRepository = tradeLogRepository;
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

    public int count() {
        return (int) tradeLogRepository.count();
    }

    public void saveAll(List<TradeLog> tradeLogs) {
        tradeLogRepository.saveAll(tradeLogs);
    }
    public TradeLogDto save(TradeLogDto tradeLogDto) {
        TradeLog savedTradeLog = tradeLogRepository.save(TradeLogDto.toEntity(tradeLogDto));
        return TradeLogDto.from(savedTradeLog);
    }
    public TradeLog save(TradeLog tradeLog) {
        return tradeLogRepository.save(tradeLog);
    }

}
