package com.back.back9.global.initData;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.log.tradeLog.entity.TradeLog;
import com.back.back9.domain.log.tradeLog.entity.TradeType;
import com.back.back9.domain.log.tradeLog.repository.TradeLogRepository;
import com.back.back9.domain.log.tradeLog.service.TradeLogService;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.wallet.entity.CoinAmount;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.back.back9.domain.wallet.repository.CoinAmountRepository;

import com.back.back9.domain.coin.repository.CoinRepository;

import com.back.back9.domain.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
            self.userCreate();
            self.walletCreate();
        };
    }
    public void userCreate() {
        userRepository.deleteAll();
        User user1 = User.builder()
                .userLoginId("user1")
                .username("유저1")
                .password("password")
                .role(User.UserRole.ADMIN)
                .build();

        User user2 = User.builder()
                .userLoginId("user2")
                .username("유저2")
                .password("password")
                .role(User.UserRole.MEMBER)
                .build();
        User user3 = User.builder()
                .userLoginId("user3")
                .username("유저3")
                .password("password")
                .role(User.UserRole.MEMBER)
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);


    }
    public void walletCreate() {
        walletRepository.deleteAll();

        // 첫 번째 유저
        User user = userRepository.findAll().get(0);
        Wallet wallet = Wallet.builder()
                .user(user)
                .address("Korea")
                .balance(new BigDecimal("10000"))
                .build();
        walletRepository.save(wallet);

        // 두 번째 유저
        User user1 = userRepository.findAll().get(1);
        Wallet wallet1 = Wallet.builder()
                .user(user1)
                .address("Korea")
                .balance(new BigDecimal("10000"))
                .build();
        walletRepository.save(wallet1);

        // 세번째 유저
        User user2 = userRepository.findAll().get(2);
        Wallet wallet2 = Wallet.builder()
                .user(user2)
                .address("Korea")
                .balance(new BigDecimal("10000"))
                .build();
        walletRepository.save(wallet2);
    }
//    public void coinAmountWork() {
//        Wallet wallet1 = walletRepository.findById(1L).orElseThrow();
//        coinRepository.deleteAll();
//
//        Coin coin1 = coinRepository.save(Coin.builder()
//                .symbol("BTC")
//                .koreanName("비트코인")
//                .englishName("Bitcoin")
//                .build());
//
////        Coin coin2 = coinRepository.save(Coin.builder()
////                .symbol("ETH")
////                .koreanName("이더리움")
////                .englishName("Ethereum")
////                .build());
//
//        CoinAmount coinAmount = CoinAmount.builder()
//                .wallet(wallet1)
//                .coin(coin1)
//                .quantity(new BigDecimal("0.05"))
//                .totalAmount(new BigDecimal("1500000"))
//                .updatedAt(OffsetDateTime.now())
//                .build();
//        coinAmountRepository.save(coinAmount);
//
//
//    }

}


