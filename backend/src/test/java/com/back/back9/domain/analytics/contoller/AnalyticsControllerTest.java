package com.back.back9.domain.analytics.contoller;

import com.back.back9.domain.analytics.controller.AnalyticsController;
import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.coin.service.CoinService;
import com.back.back9.domain.tradeLog.entity.TradeLog;
import com.back.back9.domain.tradeLog.entity.TradeType;
import com.back.back9.domain.tradeLog.repository.TradeLogRepository;
import com.back.back9.domain.tradeLog.service.TradeLogService;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.domain.wallet.entity.CoinAmount;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.CoinAmountRepository;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.back.back9.domain.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("trade_log")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class AnalyticsControllerTest {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsControllerTest.class);

    @Autowired
    private AnalyticsController analyticsController;
    @Autowired
    private TradeLogService tradeLogService;
    @Autowired
    private TradeLogRepository tradeLogRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoinService coinService;

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private CoinAmountRepository coinAmountRepository;
    @Autowired
    private MockMvc mockMvc;
    private User user1;
    private User user2;
    private User user3;

    private Coin coin1;
    private Coin coin2;
    private Coin coin3;
    private Coin coin4;

    private Wallet wallet1;
    private Wallet wallet2;
    private Wallet wallet3;

    @BeforeEach
    void setUp() {
        tradeLogRepository.deleteAll();
        coinAmountRepository.deleteAll();
        walletRepository.deleteAll();
        coinRepository.deleteAll();
        userRepository.deleteAll();

        userCreate();
        walletCreate();
        coinCreate();
        coinAmountCreate();
        tradeLogCreate();
        tradeLogChargeCreate();
    }
    public void userCreate() {
        user1 = userRepository.save(User.builder()
                .userLoginId("user1")
                .username("유저1")
                .password("password")
                .role(User.UserRole.ADMIN)
                .build());
        user2 = userRepository.save(User.builder()
                .userLoginId("user2")
                .username("유저2")
                .password("password")
                .role(User.UserRole.MEMBER)
                .build());
        user3 = userRepository.save(User.builder()
                .userLoginId("user3")
                .username("유저3")
                .password("password")
                .role(User.UserRole.MEMBER)
                .build());
    }
    public void walletCreate() {
        wallet1 = walletRepository.save(Wallet.builder()
                .user(user1)
                .address("Korea")
                .balance(BigDecimal.valueOf(500_000_000L))
                .coinAmounts(new ArrayList<>())  // null 방지
                .build());
        wallet2 = walletRepository.save(Wallet.builder()
                .user(user2)
                .address("Korea")
                .balance(BigDecimal.valueOf(500_000_000L))
                .coinAmounts(new ArrayList<>())
                .build());
        wallet3 = walletRepository.save(Wallet.builder()
                .user(user3)
                .address("Korea")
                .balance(BigDecimal.valueOf(500_000_000L))
                .coinAmounts(new ArrayList<>())
                .build());
    }
    public void tradeLogCreate() {
        if(tradeLogService.count() > 0) return;

        List<TradeLog> logs = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.of(2025, 7, 25, 0, 0);

        for (int i = 1; i <= 15; i++) {
            TradeLog log = TradeLog.builder()
                    .wallet(wallet1)
                    .coin(i <= 6 || i > 12 ? coin1 : coin2)
                    .type(i % 3 == 0 ? TradeType.SELL : TradeType.BUY)
                    .quantity(BigDecimal.ONE)
                    .price(BigDecimal.valueOf(100_000_000L + (i * 10_000_000L)))
                    .build();
            log.setCreatedAt(baseDate.plusDays((i - 1) * 7));
            logs.add(log);
        }

        tradeLogService.saveAll(logs);
    }
    public void tradeLogChargeCreate() {
        List<TradeLog> logs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            TradeLog log = TradeLog.builder()
                    .wallet(wallet1) // 실제 Wallet 객체 주입
                    .coin(coin1)     // 필요하다면 코인도 지정
                    .type(TradeType.CHARGE)
                    .quantity(BigDecimal.ONE)
                    .price(BigDecimal.valueOf(200_000_000))
                    .build();

            log.setCreatedAt(LocalDateTime.now().minusDays(3 - i)); // 생성일 세팅
            logs.add(log);
        }
        tradeLogRepository.saveAll(logs);
    }

    public void coinCreate() {

        coin1 = coinRepository.save(Coin.builder()
                .symbol("KRW-BTC2")
                .koreanName("비트코인2")
                .englishName("Bitcoin2")
                .build());

        coin2 = coinRepository.save(Coin.builder()
                .symbol("KRW-ETH2")
                .koreanName("이더리움2")
                .englishName("Ethereum2")
                .build());

        coin3 = coinRepository.save(Coin.builder()
                .symbol("KRW-XRP2")
                .koreanName("리플2")
                .englishName("Ripple2")
                .build());

        coin4 = coinRepository.save(Coin.builder()
                .symbol("KRW-DOGE2")
                .koreanName("도지코인2")
                .englishName("Dogecoin2")
                .build());
    }
    public void coinAmountCreate() {
        CoinAmount ca1 = CoinAmount.builder()
                .wallet(wallet1)
                .coin(coin1)
                .quantity(BigDecimal.valueOf(3.0))
                .totalAmount(BigDecimal.valueOf(620_000_000L))
                .build();

        CoinAmount ca2 = CoinAmount.builder()
                .wallet(wallet1)
                .coin(coin2)
                .quantity(BigDecimal.valueOf(2.0))
                .totalAmount(BigDecimal.valueOf(410_000_000L))
                .build();

        wallet1.getCoinAmounts().add(ca1);
        wallet1.getCoinAmounts().add(ca2);
        coinAmountRepository.save(ca1);
        coinAmountRepository.save(ca2);

    }
    @DisplayName("유저 실현 수익률 계산 API - 성공")
    @Test
    void t1() throws Exception {
        String url = "/api/analytics/wallet/" + wallet1.getId() + "/realized";

        ResultActions resultActions = mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AnalyticsController.class))
                .andExpect(handler().methodName("calculateRealizedProfitRates"))
                .andExpect(jsonPath("$.coinAnalytics.length()").value(2))
                .andExpect(jsonPath("$.coinAnalytics[0].coinName").value(coin1.getId()))
                .andExpect(jsonPath("$.coinAnalytics[0].totalQuantity").value(3))
                .andExpect(jsonPath("$.coinAnalytics[0].averageBuyPrice").value(165000000.0))
//                .andExpect(jsonPath("$.coinAnalytics[0].realizedProfitRate").value(closeTo(9.09090900, 0.000001)))
                .andExpect(jsonPath("$.coinAnalytics[1].coinName").value(coin2.getId()))
                .andExpect(jsonPath("$.coinAnalytics[1].totalQuantity").value(2))
                .andExpect(jsonPath("$.coinAnalytics[1].averageBuyPrice").value(190000000.0));
//                .andExpect(jsonPath("$.coinAnalytics[1].realizedProfitRate").value(closeTo(7.89473600, 0.000001)))
//                .andExpect(jsonPath("$.profitRateOnInvestment").value(closeTo(6.81818100, 0.000001)));
    }

//    @DisplayName("유저 평가 수익률 계산 API - 성공")
//    @Test
//    void  t2() throws Exception {
//        String url = "/api/analytics/wallet/" + wallet1.getId() + "/unrealized";
//
//        ResultActions resultActions = mockMvc
//                .perform(get(url).contentType(MediaType.APPLICATION_JSON))
//                .andDo(print());
//        resultActions
//                .andExpect(status().isOk())
//                .andExpect(handler().handlerType(AnalyticsController.class))
//                .andExpect(handler().methodName("calculateUnRealizedProfitRates"))
//                .andExpect(jsonPath("$.coinAnalytics.length()").value(2))
//                // 코인 1
//                .andExpect(jsonPath("$.coinAnalytics[0].coinName").value(coin1.getId()))
//                .andExpect(jsonPath("$.coinAnalytics[0].totalQuantity").value(3))
////                .andExpect(jsonPath("$.coinAnalytics[0].averageBuyPrice").value(closeTo(206666666.66666667, 0.000001)))
////                .andExpect(jsonPath("$.coinAnalytics[0].realizedProfitRate").value(closeTo(11.29032300, 0.000001)))
//                // 코인 2
//                .andExpect(jsonPath("$.coinAnalytics[1].coinName").value(coin2.getId()))
//                .andExpect(jsonPath("$.coinAnalytics[1].totalQuantity").value(2));
////                .andExpect(jsonPath("$.coinAnalytics[1].averageBuyPrice")
////                        .value(closeTo(205000000.00, 0.000001)))
////                .andExpect(jsonPath("$.coinAnalytics[1].realizedProfitRate").value(closeTo(12.19512200, 0.000001)))
//
//        // 총 수익률
////                .andExpect(jsonPath("$.profitRateOnInvestment").value(closeTo(11.65048500, 0.000001)));
//    }
}

