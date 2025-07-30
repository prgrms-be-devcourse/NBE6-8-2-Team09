package com.back.back9.domain.analytics.dto;

import java.math.BigDecimal;
import java.util.List;
// 평가/실현 수익률 응답 클라이언트용
public record ProfitRateResponse(
        int userId,
        List<ProfitAnalysisDto> coinAnalytics,
        BigDecimal totalProfitRate
){}