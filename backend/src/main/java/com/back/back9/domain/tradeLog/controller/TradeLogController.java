package com.back.back9.domain.tradeLog.controller;

import com.back.back9.domain.tradeLog.dto.TradeLogDto;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.service.TradeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 거래내역 컨트롤러
 * @author dhj
 * 거래내역은 거래가 발생할 때마다 자동으로 생성되며, 사용자가 직접 추가하거나 수정할 수 없음
 * 본인 계정의 거래내역만 조회 가능
 */
@RestController
@RequestMapping("/api/tradeLog")
@RequiredArgsConstructor
@Validated
//@SecurityRequirement(name = "bearerAuth")
public class TradeLogController {
    private final TradeLogService tradeLogService;

    @GetMapping("/wallet/{wallet_id}")
    @Transactional(readOnly = true)
    ///back9/tradeLogs?startDate=2023-10-01&endDate=2023-10-31&type=buy
    public List<TradeLogDto> getItems(
            @PathVariable Long wallet_id,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long coinId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        TradeType tradeType = null;
        if (type != null) {
            try {
                tradeType = TradeType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid trade type: " + type);
            }
        }
        List<TradeLog> items = tradeLogService.findByFilter(wallet_id, tradeType, coinId, siteId, startDateTime, endDateTime, pageable);

        return items.stream()
                .map(TradeLogDto::new)
                .toList();
    }
}
