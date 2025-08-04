package com.back.back9.domain.tradeLog.dto;

import com.back.back9.domain.tradeLog.entity.TradeLog;

import java.time.format.DateTimeFormatter;
//프론트 응답 DTO
public record TradeLogResponse(
        String date,
        String coinSymbol,
        String tradeType,
        String price,
        String quantity
) {
    public TradeLogResponse(TradeLogDto tradeLogDto) {
        this(
                tradeLogDto.createdAt().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                tradeLogDto.coinSymbol(),
                tradeLogDto.tradeType().toString(),
                tradeLogDto.price().toPlainString(),
                tradeLogDto.quantity().toPlainString()
        );
    }

}
