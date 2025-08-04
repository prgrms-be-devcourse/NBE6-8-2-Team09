package com.back.back9.domain.analytics.controller;

import com.back.back9.domain.analytics.dto.ProfitRateResponse;
import com.back.back9.domain.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/wallet/{walletId}/realized")
    public ResponseEntity<ProfitRateResponse> calculateRealizedProfitRates(@PathVariable int walletId) {
        ProfitRateResponse response = analyticsService.calculateRealizedProfitRates(walletId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}/unrealized")
    public ResponseEntity<ProfitRateResponse> calculateUnRealizedProfitRates(@PathVariable int walletId) {
        ProfitRateResponse response = analyticsService.calculateUnRealizedProfitRates(walletId);
        return ResponseEntity.ok(response);
    }
}
