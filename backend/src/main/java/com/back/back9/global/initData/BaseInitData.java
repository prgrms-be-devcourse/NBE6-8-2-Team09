package com.back.back9.global.initData;

import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.back.back9.domain.wallet.repository.CoinAmountRepository;

import com.back.back9.domain.coin.repository.CoinRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private CoinAmountRepository coinAmountRepository;
    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private UserRepository userRepository;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {

        };
    }


}


