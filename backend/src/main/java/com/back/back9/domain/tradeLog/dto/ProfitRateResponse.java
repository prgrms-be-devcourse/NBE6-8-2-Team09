package com.back.back9.domain.tradeLog.dto;

import java.util.List;

public record ProfitRateResponse(
        int userId,
        List<ProfitAnalysisDto> coinAnalytics
){}