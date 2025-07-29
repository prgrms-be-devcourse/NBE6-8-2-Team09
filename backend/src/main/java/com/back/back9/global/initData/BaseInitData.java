package com.back.back9.global.initData;

import com.back.back9.domain.coin.service.CoinService;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.service.TradeLogService;
import com.back.back9.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    @Autowired
    private final TradeLogService tradeLogService;
    @Autowired
    private final CoinService coinService;
    @Autowired
    private UserRepository userRepository;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.tradeLogWork();
        };
    }

    @Transactional
    public void coinWork() {
        if (coinService.count() > 0) return;

        coinService.add("KRW-BTC", "비트코인", "Bitcoin");
        coinService.add("KRW-ETH", "이더리움", "Ethereum");
        coinService.add("KRW-XRP", "리플", "Ripple");
        coinService.add("KRW-LTC", "라이트코인", "Litecoin");
        coinService.add("KRW-BCH", "비트코인 캐시", "Bitcoin Cash");

    }

    @Transactional
    public void tradeLogWork() {
        if(tradeLogService.count() > 0) return;

        List<TradeLog> logs = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.of(2025, 7, 25, 0, 0);

        for (int i = 1; i <= 15; i++) {
            TradeLog log = new TradeLog();

            log.setExchangeId(i);
            log.setWalletId(1);

            if(i <= 3){
                log.setCoinId(1);

            }else if(i <= 10){
//                log.setWalletId(2);
                log.setCoinId(2);
            }else{
//                log.setWalletId(3);
                log.setCoinId(3);
            }
            TradeType type = (i % 3 == 0) ? TradeType.SELL : TradeType.BUY;

            log.setType(i % 3 == 0 ? TradeType.SELL : TradeType.BUY);
//양수일경우
//            log.setQuantity(BigDecimal.valueOf(1 + (i * 0.1))); // 예: 1.1, 1.2, ...
//            log.setPrice(BigDecimal.valueOf(30000 + (i * 500))); // 예: 30500, 31000, ...
////            log.setProfitRate(BigDecimal.valueOf(0)); // 0
//            log.setCreatedAt(baseDate.plusDays((i - 1) * 7));
//            logs.add(log);
            // 음수일경우
            // 수량은 동일
            log.setQuantity(BigDecimal.valueOf(1 + (i * 0.1))); // 예: 1.1, 1.2, ...

            // 가격 정책: SELL일 경우 가격을 낮게 설정
            BigDecimal price;
            if (type == TradeType.SELL) {
                price = BigDecimal.valueOf(25000 + (i * 100)); // 낮은 가격으로 SELL
            } else {
                price = BigDecimal.valueOf(30000 + (i * 500)); // 높은 가격으로 BUY
            }
            log.setPrice(price);

//            log.setCreatedAt(baseDate.plusDays((i - 1) * 7));
            logs.add(log);
        }

        tradeLogService.saveAll(logs);
    }
}