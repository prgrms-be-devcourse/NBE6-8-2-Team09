package com.back.back9.domain.log.walletLog.entity;

import com.back.back9.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "wallet_log")
public class WalletLog extends BaseEntity {
    @NotNull
    private int userId;

    @NotNull
    private int walletId;

    @NotNull
    private TransactionType transactionType;

    @NotNull
    private BigDecimal price;

}
