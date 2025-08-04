package com.back.back9.domain.tradeLog.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
//프론트 요청 DTO
public record TradeLogRequest(
        String type,
        Integer coinId,
        Integer siteId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {}