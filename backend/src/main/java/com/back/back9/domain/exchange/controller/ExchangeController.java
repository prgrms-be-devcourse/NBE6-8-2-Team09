package com.back.back9.domain.exchange.controller;


import com.back.back9.domain.exchange.dto.CoinPriceResponse;
import com.back.back9.domain.exchange.service.ExchangeService;
import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping("/call")
    public ResponseEntity<?> getLatest(@RequestParam String symbol) {
        CoinPriceResponse response = exchangeService.getLatestCandleByScan(symbol);
        if (response == null) {
            throw new ErrorException(ErrorCode.COIN_NOT_FOUND, symbol);
        }
        return ResponseEntity.ok(response);
    }
}