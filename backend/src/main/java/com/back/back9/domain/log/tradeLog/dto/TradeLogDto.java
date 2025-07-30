package com.back.back9.domain.log.tradeLog.dto;

import com.back.back9.domain.log.tradeLog.entity.TradeLog;
import com.back.back9.domain.log.tradeLog.entity.TradeType;

import java.math.BigDecimal;

public record TradeLogDto(
        int id,
        int walletId,
        String createdAt,
        int coinId,
        TradeType tradeType,
        BigDecimal quantity,
        BigDecimal price
) {
    public TradeLogDto(TradeLog tradeLog) {
        this(
                Math.toIntExact(tradeLog.getId()),
                tradeLog.getWalletId(),
                tradeLog.getCreatedAt().toLocalDate().toString(),
                tradeLog.getCoinId(),
                tradeLog.getType(),
                tradeLog.getQuantity(),
                tradeLog.getPrice()
        );
    }

    public static TradeLogDto from(TradeLog tradeLog) {
        return new TradeLogDto(tradeLog);
    }

    public static TradeLog toEntity(TradeLogDto dto) {
        TradeLog entity = new TradeLog();
        entity.setWalletId(dto.walletId());
        entity.setCoinId(dto.coinId());
        entity.setType(dto.tradeType());
        entity.setQuantity(dto.quantity());
        entity.setPrice(dto.price());

        // createdAt은 BaseEntity에 존재 (protected)
        // BaseEntity의 setCreatedAt(LocalDateTime) 메서드가 있으면 사용
        // entity.setCreatedAt(LocalDateTime.parse(dto.createdAt())); // 예시

        return entity;
    }
}