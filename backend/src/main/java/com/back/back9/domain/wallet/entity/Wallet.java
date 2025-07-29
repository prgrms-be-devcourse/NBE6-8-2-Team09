package com.back.back9.domain.wallet.entity;


import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.user.entity.User;
import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// Wallet 엔티티
// 이 클래스는 사용자의 지갑 정보를 나타내며, 여러 코인의 수량 정보를 가질 수 있습니다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "wallet")
public class Wallet extends BaseEntity {

    @NotNull
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "tradelog_id")
    private TradeLog tradeLog;



    @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CoinAmount> coinAmounts;

    @NotNull
    private String address;

    // 기본 잔액을 5억으로 설정
    @Builder.Default
    @Column(precision = 19, scale = 8)
    private BigDecimal balance = BigDecimal.valueOf(500000000);

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // 비즈니스 메서드
    public void charge(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = OffsetDateTime.now();
    }
}
