package com.back.back9.domain.wallet.entity;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// CoinAmount 엔티티
// 코인 수량 및 정보
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "coin_amount")
public class CoinAmount extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @NotNull
    @Column(name = "quantity", precision = 19, scale = 8)
    private BigDecimal quantity;  // 코인 개수 (예: 0.005개)

    @NotNull
    @Column(name = "total_amount", precision = 19, scale = 8)
    private BigDecimal totalAmount;  // 총 투자 금액 (quantity * 평균 매수가)

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // 비즈니스 메서드
    public void updateQuantityAndAmount(BigDecimal newQuantity, BigDecimal newTotalAmount) {
        this.quantity = newQuantity;
        this.totalAmount = newTotalAmount;
        this.updatedAt = OffsetDateTime.now();
    }

    public void addQuantityAndAmount(BigDecimal additionalQuantity, BigDecimal additionalAmount) {
        this.quantity = this.quantity.add(additionalQuantity);
        this.totalAmount = this.totalAmount.add(additionalAmount);
        this.updatedAt = OffsetDateTime.now();
    }

    public void subtractQuantityAndAmount(BigDecimal subtractQuantity, BigDecimal subtractAmount) {
        this.quantity = this.quantity.subtract(subtractQuantity);
        this.totalAmount = this.totalAmount.subtract(subtractAmount);
        this.updatedAt = OffsetDateTime.now();
    }

    // 평균 매수 단가 계산 (총 투자금액 / 보유 수량)
    public BigDecimal getAverageBuyPrice() {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount.divide(quantity, 8, java.math.RoundingMode.HALF_UP);
    }

    // 기존 메서드들은 Deprecated 처리
    @Deprecated
    public void updateAmount(BigDecimal newAmount) {
        this.totalAmount = newAmount;
        this.updatedAt = OffsetDateTime.now();
    }

    @Deprecated
    public void addAmount(BigDecimal additionalAmount) {
        this.totalAmount = this.totalAmount.add(additionalAmount);
        this.updatedAt = OffsetDateTime.now();
    }

    @Deprecated
    public void subtractAmount(BigDecimal subtractAmount) {
        this.totalAmount = this.totalAmount.subtract(subtractAmount);
        this.updatedAt = OffsetDateTime.now();
    }
}
