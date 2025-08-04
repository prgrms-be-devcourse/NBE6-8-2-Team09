package com.back.back9.domain.tradeLog.controller;

import com.back.back9.domain.tradeLog.dto.TradeLogDto;
import com.back.back9.domain.tradeLog.dto.TradeLogRequest;
import com.back.back9.domain.tradeLog.dto.TradeLogResponse;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.service.TradeLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
public class TradeLogController {
    private final TradeLogService tradeLogService;
    private static final Logger log = (Logger) org.slf4j.LoggerFactory.getLogger(TradeLogController.class);

    @GetMapping("/wallet/{wallet_id}")
    ///back9/tradeLogs?startDate=2023-10-01&endDate=2023-10-31&type=buy
    public ResponseEntity<List<TradeLogResponse>> getItems(
            @PathVariable("wallet_id") int walletId,
            @ModelAttribute TradeLogRequest request,
            Pageable pageable
    ) {
        LocalDateTime startDateTime = request.startDate() != null ? request.startDate().atStartOfDay() : null;
        LocalDateTime endDateTime = request.endDate() != null ? request.endDate().atTime(LocalTime.MAX) : null;

        TradeType tradeType = null;
        if (request.type() != null) {
            try {
                tradeType = TradeType.valueOf(request.type().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid trade type: " + request.type());
            }
        }

        List<TradeLogDto> items = tradeLogService.findByFilter(
                walletId,
                tradeType,
                request.coinId(),
                startDateTime,
                endDateTime,
                pageable
        );

        List<TradeLogResponse> result = items.stream()
                .map(TradeLogResponse::new)
                .toList();

        log.info("거래내역 조회 - 지갑 ID: {}, 거래 유형: {}, 코인 ID: {}, 시작일: {}, 종료일: {}, 페이지: {}",
                walletId, request.type(), request.coinId(), request.startDate(), request.endDate(), pageable.getPageNumber());

        return ResponseEntity.ok(result);
    }
    @PostMapping("/mock")
    public ResponseEntity<?> createMockTradeLogs() {
        tradeLogService.createMockLogs();
        return ResponseEntity.ok("Mock trade logs created.");
    }
}
