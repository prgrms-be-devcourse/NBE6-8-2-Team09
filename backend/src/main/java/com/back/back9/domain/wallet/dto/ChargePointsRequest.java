package com.back.back9.domain.wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;


// 충전 포인트 요청 DTO
public record ChargePointsRequest(
        @NotNull(message = "충전할 포인트는 필수입니다")
        @Positive(message = "충전할 포인트는 양수여야 합니다")
        BigDecimal amount
) {
    public BigDecimal getAmount() {
        return amount;
    }
}
