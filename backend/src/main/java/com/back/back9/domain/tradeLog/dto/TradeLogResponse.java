package com.back.back9.domain.tradeLog.dto;

import com.back.back9.domain.tradeLog.entity.TradeLog;

import java.time.format.DateTimeFormatter;

public record TradeLogResponse(
        String date,
        String coinSymbol,
        String tradeType,
        String price,
        String quantity
) {
    public TradeLogResponse(TradeLog tradeLog) {
        this(
                tradeLog.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                tradeLog.getCoin().getSymbol(),
                tradeLog.getType().toString(),
                tradeLog.getPrice().toPlainString(),
                tradeLog.getQuantity().toPlainString()
        );
    }

}
