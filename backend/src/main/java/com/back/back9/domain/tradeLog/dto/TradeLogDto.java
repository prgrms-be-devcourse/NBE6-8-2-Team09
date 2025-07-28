package com.back.back9.domain.tradeLog.dto;

import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;

import java.math.BigDecimal;

public record TradeLogDto(
    int id,
    String date,
    int coinId,
    TradeType tradeType,
    BigDecimal quantity, //거래 수량
    BigDecimal price //구매한 금액
) {
    public TradeLogDto(TradeLog tradeLog){
        this(
                tradeLog.getId(),
                tradeLog.getCreatedAt().toLocalDate().toString(),
                tradeLog.getCoinId(),
                tradeLog.getType(),
                tradeLog.getQuantity(),
                tradeLog.getPrice()
        );
    }
}
