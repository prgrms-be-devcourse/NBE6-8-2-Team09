package com.back.back9.domain.log.walletLog.service;

import com.back.back9.domain.log.walletLog.dto.WalletLogDto;
import com.back.back9.domain.log.walletLog.repository.WalletLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletLogService {
    private final WalletLogRepository walletLogRepository;

    public WalletLogService(WalletLogRepository walletLogRepository) {
        this.walletLogRepository = walletLogRepository;
    }

    public List<WalletLogDto> findByWalletId(int walletId) {
        return walletLogRepository.findByWalletId(walletId)
                .stream()
                .map(WalletLogDto::from)
                .toList();
    }
}
