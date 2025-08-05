package com.back.back9.domain.order.dto;

import com.back.back9.domain.order.entity.OrderMethod;
import com.back.back9.domain.tradeLog.entity.TradeType;

import java.math.BigDecimal;
//프론트 요청 DTO
public record OrderRequest(
        int coinId,
        TradeType tradeType, // BUY, SELL
        OrderMethod orderMethod, // LIMIT, MARKET
        BigDecimal quantity,
        BigDecimal price
) {

}
