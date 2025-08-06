package com.back.back9.domain.exchange.controller;

import com.back.back9.domain.exchange.dto.ExchangeDTO;
import com.back.back9.domain.exchange.dto.InitialRequestDTO;
import com.back.back9.domain.exchange.dto.PreviousDTO;
import com.back.back9.domain.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping("/initial")
    public List<ExchangeDTO> getInitialCandles(@RequestBody InitialRequestDTO request) {
        //InitialRequestDTO { private CandleInterval interval(캔들 단위); private String market(코인 심볼); }
        //캔들 정보 170개 호출
        return exchangeService.getInitialCandles(request.getInterval(), request.getMarket());
    }

    @PostMapping("/previous")
    public List<ExchangeDTO> getPreviousCandles(@RequestBody PreviousDTO request) {
        //public class PreviousDTO { private CandleInterval interval; private String market; private int page; private long timestamp; private LocalDateTime time; }
        // 기준으로 캔들정보 50개씩 페이징으로 호출
        return exchangeService.getPreviousCandles(request.getInterval(), request.getMarket(), request.getPage(), request.getTime());
    }

    @GetMapping("/coins-latest")
    public List<ExchangeDTO> getCoinList(){
        return exchangeService.getCoinsLatest();
    }
}