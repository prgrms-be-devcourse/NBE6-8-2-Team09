package com.back.back9.domain.analytics.contoller;

import com.back.back9.domain.analytics.controller.AnalyticsController;
import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.coin.service.CoinService;
import com.back.back9.domain.log.tradeLog.entity.TradeLog;
import com.back.back9.domain.log.tradeLog.entity.TradeType;
import com.back.back9.domain.log.tradeLog.repository.TradeLogRepository;
import com.back.back9.domain.log.tradeLog.service.TradeLogService;
import com.back.back9.domain.log.walletLog.entity.TransactionType;
import com.back.back9.domain.log.walletLog.entity.WalletLog;
import com.back.back9.domain.log.walletLog.repository.WalletLogRepository;
import com.back.back9.domain.log.walletLog.service.WalletLogService;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.user.service.UserService;
import com.back.back9.domain.wallet.entity.CoinAmount;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
import com.back.back9.domain.wallet.service.WalletService;
import com.back.back9.domain.wallet.repository.CoinAmountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private WalletLogService walletLogService;
    @Autowired
    private WalletLogRepository walletLogRepository;

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
    private User testUser;
    private Coin coin1;
    private Coin coin2;
    private Coin coin3;
    private Coin coin4;
    private Wallet wallet;

    @BeforeEach
    void setUp() {

        this.testUser = userRepository.findByUserLoginId("user1")
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        this.wallet = walletRepository.findByUser(testUser)
                .orElseThrow(() -> new RuntimeException("지갑 없음"));
        coinCreate();
        coinAmountCreate();
        tradeLogCreate();
        walletLogCreate();
    }

    public void tradeLogCreate() {
        tradeLogRepository.deleteAll();
        if(tradeLogService.count() > 0) return;

        List<TradeLog> logs = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.of(2025, 7, 25, 0, 0);

        for (int i = 1; i <= 15; i++) {
            TradeLog log = new TradeLog();

            if(i <= 6){
                log.setWalletId(1);
                log.setCoinId(1);

            }else if(i <= 12){
                log.setWalletId(1);
                log.setCoinId(2);
            }else{
                log.setWalletId(1);
                log.setCoinId(1);
            }
            TradeType type = (i % 3 == 0) ? TradeType.SELL : TradeType.BUY;

            log.setType(i % 3 == 0 ? TradeType.SELL : TradeType.BUY);

            log.setCreatedAt(baseDate.plusDays((i - 1) * 7));
            logs.add(log);

            log.setQuantity(BigDecimal.valueOf(1));

            BigDecimal price = BigDecimal.valueOf(100_000_000L + (i * 10_000_000L));
            log.setPrice(price);

        }

        tradeLogService.saveAll(logs);
    }
    public void walletLogCreate() {
        walletLogRepository.deleteAll();      // 예시용 유저, 지갑 ID (1번째 유저/지갑 기준)
        int userId = 1;
        int walletId = 1;

        List<WalletLog> logs = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            WalletLog log = WalletLog.builder()
                    .userId(userId)
                    .walletId(walletId)
                    .transactionType(TransactionType.CHARGE) // 또는 WITHDRAW
                    .price(BigDecimal.valueOf(200000000)) // 만원씩 충전
                    .build();
            logs.add(log);
        }

        walletLogRepository.saveAll(logs);
    }

    public void coinCreate() {
        coinRepository.deleteAll();

        coin1 = coinRepository.save(Coin.builder()
                .symbol("KRW-BTC")
                .koreanName("비트코인")
                .englishName("Bitcoin")
                .build());

        coin2 = coinRepository.save(Coin.builder()
                .symbol("KRW-ETH")
                .koreanName("이더리움")
                .englishName("Ethereum")
                .build());

        coin3 = coinRepository.save(Coin.builder()
                .symbol("KRW-XRP")
                .koreanName("리플")
                .englishName("Ripple")
                .build());

        coin4 = coinRepository.save(Coin.builder()
                .symbol("KRW-DOGE")
                .koreanName("도지코인")
                .englishName("Dogecoin")
                .build());
    }
    public void coinAmountCreate() {
        coinAmountRepository.deleteAll();
        CoinAmount coinAmount = CoinAmount.builder()
                .wallet(wallet) // wallet 필드에 세팅
                .coin(coin1)         // coin 필드에 세팅
                .quantity(BigDecimal.valueOf(3.0))     // 예: 3개 보유
                .totalAmount(BigDecimal.valueOf(620_000_000L))
                .build();
        CoinAmount coinAmount1 = CoinAmount.builder()
                .wallet(wallet) // wallet 필드에 세팅
                .coin(coin2)         // coin 필드에 세팅
                .quantity(BigDecimal.valueOf(2.0))     // 예: 2개 보유
                .totalAmount(BigDecimal.valueOf(410_000_000L))
                .build();
        wallet.getCoinAmounts().add(coinAmount);
        wallet.getCoinAmounts().add(coinAmount1);
        coinAmountRepository.save(coinAmount);
        coinAmountRepository.save(coinAmount1);

    }
    @DisplayName("유저 실현 수익률 계산 API - 성공")
    @Test
    void t1() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(get("/api/analytics/wallet/1/realized")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AnalyticsController.class))
                .andExpect(handler().methodName("calculateRealizedProfitRates"))
                .andExpect(jsonPath("$.coinAnalytics.length()").value(2))
                .andExpect(jsonPath("$.coinAnalytics[0].coinId").value(coin1.getId()))
                .andExpect(jsonPath("$.coinAnalytics[0].totalQuantity").value(3))
                .andExpect(jsonPath("$.coinAnalytics[0].averageBuyPrice").value(165000000.0))
                .andExpect(jsonPath("$.coinAnalytics[0].realizedProfitRate").value(closeTo(4.54545454, 0.000001)))
                .andExpect(jsonPath("$.coinAnalytics[1].coinId").value(coin2.getId()))
                .andExpect(jsonPath("$.coinAnalytics[1].totalQuantity").value(2))
                .andExpect(jsonPath("$.coinAnalytics[1].averageBuyPrice").value(190000000.0))
                .andExpect(jsonPath("$.coinAnalytics[1].realizedProfitRate").value(closeTo(3.94736842, 0.000001)))
                .andExpect(jsonPath("$.totalProfitRate").value(closeTo(6.81818181, 0.000001)));
    }

    @DisplayName("유저 평가 수익률 계산 API - 성공")
    @Test
    void  t2() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(get("/api/analytics/wallet/1/Unrealized")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AnalyticsController.class))
                .andExpect(handler().methodName("calculateUnRealizedProfitRates"))
                .andExpect(jsonPath("$.coinAnalytics.length()").value(2))
                // 코인 1
                .andExpect(jsonPath("$.coinAnalytics[0].coinId").value(coin1.getId()))
                .andExpect(jsonPath("$.coinAnalytics[0].totalQuantity").value(3))
                .andExpect(jsonPath("$.coinAnalytics[0].averageBuyPrice").value(closeTo(206666666.66666667, 0.000001)))
                .andExpect(jsonPath("$.coinAnalytics[0].realizedProfitRate").value(closeTo(11.29032300, 0.000001)))
                // 코인 2
                .andExpect(jsonPath("$.coinAnalytics[1].coinId").value(coin2.getId()))
                .andExpect(jsonPath("$.coinAnalytics[1].totalQuantity").value(2))
                .andExpect(jsonPath("$.coinAnalytics[1].averageBuyPrice")
                        .value(closeTo(205000000.00, 0.000001)))
                .andExpect(jsonPath("$.coinAnalytics[1].realizedProfitRate").value(closeTo(12.19512200, 0.000001)))
                // 총 수익률
                .andExpect(jsonPath("$.totalProfitRate").value(closeTo(11.65048500, 0.000001)));
    }
}
