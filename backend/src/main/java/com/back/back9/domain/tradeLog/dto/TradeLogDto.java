package com.back.back9.domain.tradeLog.dto;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//내부용 DTO
public record TradeLogDto(
        int id,
        int walletId,
        LocalDateTime createdAt,
        int coinId,
        String coinSymbol,
        TradeType tradeType,
        BigDecimal quantity,
        BigDecimal price
) {
    public TradeLogDto(TradeLog tradeLog) {
        this(
                Math.toIntExact(tradeLog.getId()),
                tradeLog.getWallet().getId().intValue(),
                tradeLog.getCreatedAt(),
                tradeLog.getCoin().getId().intValue(),
                tradeLog.getCoin().getSymbol(),
                tradeLog.getType(),
                tradeLog.getQuantity(),
                tradeLog.getPrice()
        );
    }

    public static TradeLogDto from(TradeLog tradeLog) {
        return new TradeLogDto(tradeLog);
    }

    public static TradeLog toEntity(TradeLogDto dto, Wallet wallet, Coin coin) {
        TradeLog entity = new TradeLog();
        entity.setWallet(wallet);
        entity.setCoin(coin);
        entity.setType(dto.tradeType());
        entity.setQuantity(dto.quantity());
        entity.setPrice(dto.price());

        // createdAt은 BaseEntity에 존재 (protected)
        // BaseEntity의 setCreatedAt(LocalDateTime) 메서드가 있으면 사용
        // entity.setCreatedAt(LocalDateTime.parse(dto.createdAt())); // 예시

        return entity;
    }
}