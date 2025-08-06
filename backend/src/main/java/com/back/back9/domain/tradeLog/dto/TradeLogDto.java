package com.back.back9.domain.tradeLog.dto;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//내부용 DTO
public record TradeLogDto(
        Long id,
        Long walletId,
        LocalDateTime createdAt,
        Long coinId,
        String coinSymbol,
        TradeType tradeType,
        BigDecimal quantity,
        BigDecimal price
) {
    public TradeLogDto(TradeLog tradeLog) {
        this(
                tradeLog.getId(),
                tradeLog.getWallet().getId(),
                tradeLog.getCreatedAt(),
                tradeLog.getCoin().getId(),
                tradeLog.getCoin().getSymbol(),
                tradeLog.getType(),
                tradeLog.getQuantity(),
                tradeLog.getPrice()
        );
    }

    public static TradeLogDto from(TradeLog tradeLog) {
        Long coinId = tradeLog.getCoin() != null ? tradeLog.getCoin().getId() : null;
        String coinSymbol = tradeLog.getCoin() != null ? tradeLog.getCoin().getSymbol() : null;

        return new TradeLogDto(
                tradeLog.getId(),
                tradeLog.getWallet().getId(),
                tradeLog.getCreatedAt(),
                coinId != null ? coinId : -1, // -1 또는 다른 기본값, 혹은 wrapper 타입으로 바꾸기
                coinSymbol,
                tradeLog.getType(),
                tradeLog.getQuantity(),
                tradeLog.getPrice()
        );
    }
    public static TradeLog toEntity(TradeLogDto dto, Wallet wallet, Coin coin) {
        return TradeLog.builder()
                .wallet(wallet)
                .coin(coin)
                .type(dto.tradeType())
                .quantity(dto.quantity())
                .price(dto.price())
                .build();

    }
}