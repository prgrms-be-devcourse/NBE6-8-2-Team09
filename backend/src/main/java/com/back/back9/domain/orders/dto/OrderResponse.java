package com.back.back9.domain.orders.dto;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.orders.entity.Orders;

import java.math.BigDecimal;

public record OrderResponse(
        Long coinId,
        String coinSymbol,
    String coinName,
    String orderMethod,
    String orderStatus,
    String tradeType,
    BigDecimal price,
    BigDecimal quantity,
    String createdAt
) {
    public static OrderResponse from(Orders order) {
        Coin coin = order.getCoin();
        return new OrderResponse(
                coin.getId(),
                coin.getSymbol(),
                coin.getKoreanName(),
                order.getOrdersMethod().name(),
                order.getOrdersStatus().name(),
                order.getTradeType().name(),
                order.getPrice(),
                order.getQuantity(),
                order.getCreatedAt().toString()
        );
    }
}
