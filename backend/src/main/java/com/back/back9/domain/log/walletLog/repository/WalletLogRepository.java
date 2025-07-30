package com.back.back9.domain.log.walletLog.repository;

import com.back.back9.domain.log.walletLog.entity.WalletLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletLogRepository extends JpaRepository<WalletLog, Integer> {
    List<WalletLog> findByWalletId(int walletId);
}
