package com.back.back9.domain.tradeLog.repository;

import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
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

    List<TradeLog> findByWalletId(Long walletId);

    Page<TradeLog> findByWalletId(Long walletId, Pageable pageable);

    @Query(
            "SELECT t FROM TradeLog t " +
                    "WHERE t.wallet.id = :walletId " +
                    "  AND t.type = COALESCE(:type, t.type) " +
                    "  AND (:coinId IS NULL OR t.coin.id = :coinId) " +
                    "  AND t.createdAt >= COALESCE(:startDate, t.createdAt) " +
                    "  AND t.createdAt <= COALESCE(:endDate, t.createdAt)"
    )
    Page<TradeLog> findByWalletIdFilter(
            @Param("walletId") Long walletId,
            @Param("type")       TradeType       type,
            @Param("coinId")     Integer         coinId,
            @Param("startDate")  LocalDateTime   startDate,
            @Param("endDate")    LocalDateTime   endDate,
            Pageable             pageable
    );
}
