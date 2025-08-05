package com.back.back9.domain.tradeLog.entity;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeLog extends BaseEntity {
    //거래한 지갑
    @NotNull
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
    //거래한 거래소
//    @NotNull
//    @Column(name = "exchange_id")
//    private int exchangeId;

    //거래한 코인
    @ManyToOne
    @JoinColumn(name = "coin_id", nullable = true)
    private Coin coin;

    //거래 타입 : BUY, SELL,CHARGE
    @Enumerated
    @Column(nullable = false)
    private TradeType type;

    // 수량
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    // 단가
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;
    //DB 저장 안함
//    @Column(nullable = false, precision = 19, scale = 8)
//    private BigDecimal profitRate;

    public void setCreatedAt(LocalDateTime createdAt) {
        super.createdAt = createdAt; // BaseEntity의 protected createdAt 필드 접근
    }
}