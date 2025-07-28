package com.back.back9.domain.tradeLog.service;

import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.repository.TradeLogRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public List<TradeLog> findByFilter(Long walletId, TradeType type, Long coinId, Long siteId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (type == null && coinId == null && siteId == null && startDate == null && endDate == null) {
            return tradeLogRepository.findByWalletId(walletId, pageable).getContent();
        }

        return tradeLogRepository.findByWalletIdFilter(walletId, type, coinId, siteId, startDate, endDate, pageable).getContent();
    }

    public int count() {
        return (int) tradeLogRepository.count();
    }

    public void saveAll(List<TradeLog> tradeLogs) {
        tradeLogRepository.saveAll(tradeLogs);
    }
    public TradeLog save(TradeLog tradeLog) {
        return tradeLogRepository.save(tradeLog);
    }
}
