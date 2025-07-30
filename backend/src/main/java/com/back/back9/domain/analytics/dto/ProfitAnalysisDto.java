package com.back.back9.domain.analytics.dto;

import java.math.BigDecimal;
//평가/실현 수익률 DTO 내부 연산용
public record ProfitAnalysisDto(
        int coinId,
        BigDecimal totalQuantity,
        BigDecimal averageBuyPrice,
        BigDecimal realizedProfitRate
) {}
