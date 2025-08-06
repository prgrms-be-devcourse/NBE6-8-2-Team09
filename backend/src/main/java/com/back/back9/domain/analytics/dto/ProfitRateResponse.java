package com.back.back9.domain.analytics.dto;

import java.math.BigDecimal;
import java.util.List;
// 평가/실현 수익률 응답 클라이언트용
public record ProfitRateResponse(
        Long walletId,
        List<ProfitAnalysisDto> coinAnalytics,
        BigDecimal profitRateOnInvestment , // 투자 대비 수익률
        BigDecimal profitRateOnTotalAssets //총 자산 대비 수익률
){}