package com.back.back9.domain.wallet.dto;

import com.back.back9.domain.wallet.entity.CoinAmount;

import java.math.BigDecimal;

// 코인 보유 정보 DTO - TradeLog 서비스에서 평가수익률 계산에 사용
public record CoinHoldingInfo(
        int coinId,
        String coinSymbol,
        String coinName,
        BigDecimal quantity,           // 코인 개수 (예: 0.005개)
        BigDecimal totalInvestAmount,  // 총 투자 금액 (매수에 사용한 실제 금액)
        BigDecimal averageBuyPrice     // 평균 매수 단가 (totalInvestAmount / quantity)
) {
    // CoinAmount에서 CoinHoldingInfo로 변환 (시세 정보는 Exchange에서 별도 제공)
    public static CoinHoldingInfo from(CoinAmount coinAmount) {
        return new CoinHoldingInfo(
                coinAmount.getCoin().getId(),
                coinAmount.getCoin().getSymbol(),
                coinAmount.getCoin().getKoreanName() != null ?
                    coinAmount.getCoin().getKoreanName() : coinAmount.getCoin().getSymbol(),
                coinAmount.getQuantity(),
                coinAmount.getTotalAmount(),
                coinAmount.getAverageBuyPrice()  // 엔티티에서 계산한 평균 매수가
        );
    }
}
