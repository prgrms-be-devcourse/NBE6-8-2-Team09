package com.back.back9.domain.tradeLog.controller;

import com.back.back9.domain.tradeLog.dto.ProfitRateResponse;
import com.back.back9.domain.tradeLog.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/wallet/{walletId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProfitRateResponse> getUserProfitRate(@PathVariable int walletId) {
        ProfitRateResponse response = analyticsService.calculateRealizedProfitRates(walletId);
        return ResponseEntity.ok(response);
    }
}
