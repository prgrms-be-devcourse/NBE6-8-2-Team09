package com.back.back9.domain.log.tradeLog.repository;

import com.back.back9.domain.log.tradeLog.entity.TradeLog;
import com.back.back9.domain.log.tradeLog.entity.TradeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TradeLogRepository extends JpaRepository<TradeLog, Long> {
    Optional<TradeLog> findFirstByOrderByIdDesc();
    List<TradeLog> findByWalletId(int walletId);
    Page<TradeLog> findByWalletId(int walletId, Pageable pageable);
    @Query("SELECT t FROM TradeLog t " +
            "WHERE t.walletId = :walletId " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:coinId IS NULL OR t.coinId = :coinId) " +
            "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<TradeLog> findByWalletIdFilter(@Param("walletId") int walletId,
                                        @Param("type") TradeType type, // ← enum으로 변경
                                        @Param("coinId") Integer  coinId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);

}
