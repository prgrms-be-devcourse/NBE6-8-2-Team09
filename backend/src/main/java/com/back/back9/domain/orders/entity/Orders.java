package com.back.back9.domain.orders.entity;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Orders extends BaseEntity
{
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType; // BUY, SELL

    @Enumerated(EnumType.STRING)
    private OrdersMethod ordersMethod; // LIMIT, MARKET

    private BigDecimal quantity; // 수량

    private BigDecimal price; // 지정가 주문 시 사용

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OrdersStatus ordersStatus; // PENDING, FILLED, CANCELLED 등
}
