package com.back.back9.domain.orders.dto;

import com.back.back9.domain.orders.entity.OrdersMethod;
import com.back.back9.domain.tradeLog.entity.TradeType;

import java.math.BigDecimal;
//프론트 요청 DTO
public record OrdersRequest(
        String coinSymbol, // 코인 심볼
        String coinName,
        TradeType tradeType, // BUY, SELL
        OrdersMethod ordersMethod, // LIMIT, MARKET
        BigDecimal quantity, //수량
        BigDecimal price //단가
) {

}
