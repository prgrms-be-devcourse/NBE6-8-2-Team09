package com.back.back9.domain.websocket.service;

import com.back.back9.domain.websocket.vo.CandleInterval;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpbitRestScheduler {

    private final UpbitRestCandleFetcher restFetcher;

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void scheduledFetchRest() {
        restFetcher.fetchInterval(CandleInterval.MIN_1, 200);
        restFetcher.fetchInterval(CandleInterval.MIN_30, 200);
        restFetcher.fetchInterval(CandleInterval.HOUR_1, 200);
        restFetcher.fetchInterval(CandleInterval.DAY, 200);
        restFetcher.fetchInterval(CandleInterval.WEEK, 100);
        restFetcher.fetchInterval(CandleInterval.MONTH, 50);
        restFetcher.fetchInterval(CandleInterval.YEAR, 10);
    }
}