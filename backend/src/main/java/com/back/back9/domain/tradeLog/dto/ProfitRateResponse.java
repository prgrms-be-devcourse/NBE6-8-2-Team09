package com.back.back9.domain.tradeLog.dto;

import java.util.List;

public record ProfitRateResponse(
        Long userId,
        List<ProfitAnalysisDto> coinAnalytics
){}