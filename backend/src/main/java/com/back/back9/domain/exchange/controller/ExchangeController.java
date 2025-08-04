package com.back.back9.domain.exchange.controller;

import com.back.back9.domain.exchange.dto.ExchangeDTO;
import com.back.back9.domain.exchange.service.ExchangeService;
import com.back.back9.domain.websocket.vo.CandleInterval;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping("/initial")
    public List<ExchangeDTO> getInitialCandles(
            @RequestParam CandleInterval interval,
            @RequestParam String market
    ) {
        return exchangeService.getInitialCandles(interval, market);
    }

    @GetMapping("/previous")
    public List<ExchangeDTO> getPreviousCandles(
            @RequestParam CandleInterval interval,
            @RequestParam String market,
            @RequestParam int page,
            @RequestParam LocalDateTime time
    ) {
        return exchangeService.getPreviousCandles(interval, market, page, time);
    }
}