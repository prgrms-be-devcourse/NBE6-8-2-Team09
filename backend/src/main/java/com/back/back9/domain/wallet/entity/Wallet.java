package com.back.back9.domain.wallet.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Wallet 엔티티
// 이 클래스는 사용자의 지갑 정보를 나타내며, 잔액 충전 기능을 포함합니다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(name = "user_id")
    private int userId;

    @NotNull
    @Column(name = "coin_id")
    private int coinId;

    @NotNull
    private String address;

    // 기본 잔액을 5억으로 설정
    @Builder.Default
    @Column(precision = 19, scale = 8)
    private BigDecimal balance = BigDecimal.valueOf(500000000);

    @Builder.Default
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // 비즈니스 메서드
    public void charge(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = OffsetDateTime.now();
    }

}
