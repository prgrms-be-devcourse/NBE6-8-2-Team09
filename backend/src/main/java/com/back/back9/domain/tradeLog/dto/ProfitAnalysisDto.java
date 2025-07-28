package com.back.back9.domain.tradeLog.dto;

import java.math.BigDecimal;

public record ProfitAnalysisDto(
        int coinId,
        BigDecimal totalQuantity,
        BigDecimal averageBuyPrice,
        BigDecimal realizedProfitRate
) {}
