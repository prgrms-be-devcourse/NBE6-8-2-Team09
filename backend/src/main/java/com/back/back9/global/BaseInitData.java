package com.back.back9.global;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final CoinService coinService;


    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            work1();
        };
    }

    @Transactional
    void work1(){
        if (coinService.count() > 0) return;

        Coin coin1 = coinService.add("BTC", "비트코인","Bitcoin");
        Coin coin2 = coinService.add("DOGE", "도지코인","Dogecoin");
        Coin coin3 = coinService.add("ETH", "이더리움","Ethereum");
    }
}