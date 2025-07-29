package com.back.back9.domain.wallet.dto;

import com.back.back9.domain.wallet.entity.CoinAmount;

import java.math.BigDecimal;

// 코인별 수량 정보 DTO
public record CoinAmountResponse(
        int coinId,
        String coinSymbol,
        String coinName,
        BigDecimal quantity,      // 코인 개수 (예: 0.005개)
        BigDecimal totalAmount    // 총 투자 금액
) {
    public static CoinAmountResponse from(CoinAmount coinAmount) {
        var coin = coinAmount.getCoin();

        return new CoinAmountResponse(
                coin.getId(),
                coin.getSymbol(),
                coin.getKoreanName() != null ? coin.getKoreanName() : coin.getSymbol(),
                coinAmount.getQuantity(),
                coinAmount.getTotalAmount()
        );
    }
}
