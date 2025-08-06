package com.back.back9.domain.order.entity;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.user.entity.User;
import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Order extends BaseEntity
{
    @ManyToOne
    private User user;

    @ManyToOne
    private Coin coin;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType; // BUY, SELL

    @Enumerated(EnumType.STRING)
    private OrderMethod orderMethod; // LIMIT, MARKET

    private BigDecimal quantity; // 수량

    private BigDecimal price; // 지정가 주문 시 사용

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // PENDING, FILLED, CANCELLED 등
}
