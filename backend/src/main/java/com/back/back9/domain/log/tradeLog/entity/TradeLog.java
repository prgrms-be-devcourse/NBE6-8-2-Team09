package com.back.back9.domain.log.tradeLog.entity;

import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TradeLog extends BaseEntity {
    //거래한 지갑
    @NotNull
    @Column(name = "wallet_id")
    private int walletId;
    //거래한 거래소
//    @NotNull
//    @Column(name = "exchange_id")
//    private int exchangeId;

    //거래한 코인
    @NotNull
    @Column(name = "coin_id")
    private int coinId;

    //거래 타입 : BUY, SELL
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
