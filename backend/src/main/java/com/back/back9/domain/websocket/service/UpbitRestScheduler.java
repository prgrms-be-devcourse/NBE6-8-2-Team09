package com.back.back9.domain.websocket.service;

import com.back.back9.domain.websocket.vo.CandleInterval;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpbitRestScheduler {

    private final UpbitRestCandleFetcher restFetcher;
    //매 10분마다 스케쥴이 활성화 되어있는지 로그로 체크
    @Scheduled(fixedRate = 600_000)
    public void checkScheduler() {
        System.out.println("Scheduler 동작중");
    }

    // 매 30분마다 실행 (예: 00:00, 00:30, 01:00, ...)
    @Scheduled(cron = "0 0/30 * * * *")
    public void fetchEvery30Min() {
        restFetcher.fetchInterval(CandleInterval.MIN_30, 200);
    }

    // 매 정각 10분 후 실행 (예: 00:10, 01:10, 02:10, ...)
    @Scheduled(cron = "0 10 * * * *")
    public void fetchHourly() {
        restFetcher.fetchInterval(CandleInterval.HOUR_1, 200);
    }

    // 매일 오전 00:15 실행
    @Scheduled(cron = "0 15 0 * * *")
    public void fetchDaily() {
        restFetcher.fetchInterval(CandleInterval.DAY, 200);
    }

    // 매주 월요일 오전 01:00 실행
    @Scheduled(cron = "0 0 1 * * MON")
    public void fetchWeekly() {
        restFetcher.fetchInterval(CandleInterval.WEEK, 100);
    }

    // 매달 1일 오전 01:30 실행
    @Scheduled(cron = "0 30 1 1 * *")
    public void fetchMonthly() {
        restFetcher.fetchInterval(CandleInterval.MONTH, 50);
    }

    // 매년 1월 1일 오전 02:00 실행
    @Scheduled(cron = "0 0 2 1 1 *")
    public void fetchYearly() {
        restFetcher.fetchInterval(CandleInterval.YEAR, 10);
    }
}