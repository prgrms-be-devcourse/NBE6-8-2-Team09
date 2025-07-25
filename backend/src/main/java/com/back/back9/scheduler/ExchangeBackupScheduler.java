package com.back.back9.scheduler;

import com.back.back9.domain.exchange.service.ExchangeBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeBackupScheduler {

    private final ExchangeBackupService backupService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00
    public void scheduleDailyBackup() {
        backupService.backupPreviousDayRedisToDB();
    }
}