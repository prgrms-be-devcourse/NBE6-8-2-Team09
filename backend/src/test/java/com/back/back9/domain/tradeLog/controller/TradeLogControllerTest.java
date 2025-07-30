package com.back.back9.domain.tradeLog.controller;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.log.tradeLog.controller.TradeLogController;
import com.back.back9.domain.log.tradeLog.entity.TradeLog;
import com.back.back9.domain.log.tradeLog.entity.TradeType;
import com.back.back9.domain.log.tradeLog.repository.TradeLogRepository;
import com.back.back9.domain.log.tradeLog.service.TradeLogService;
import com.back.back9.domain.user.entity.User;
import com.back.back9.domain.user.repository.UserRepository;
import com.back.back9.domain.wallet.entity.Wallet;
import com.back.back9.domain.wallet.repository.WalletRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("trade_log")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class TradeLogControllerTest {
    @Autowired
    private TradeLogController tradeLogController;
    @Autowired
    private TradeLogService tradeLogService;
    @Autowired
    private TradeLogRepository tradeLogRepository;

    @Autowired
    private MockMvc mock;
    /*
     * 거래 로그 필터 테스트용
     * 생성 날짜 의도적으로 2025년 7월 25일로 설정, 일주일 마다 구매 하여 총 15번 구매
     * 구매 수량은 0~1사이의 소수점 8자리
     * setCreatedAt() 메서드로 날짜 설정
     */
    @BeforeEach
    void setUp() {
        tradeLogCreate();
    }


    public void tradeLogCreate() {
        tradeLogRepository.deleteAll();
        if(tradeLogService.count() > 0) return;

        List<TradeLog> logs = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.of(2025, 7, 25, 0, 0);

        for (int i = 1; i <= 15; i++) {
            TradeLog log = new TradeLog();

            if(i <= 5){
                log.setWalletId(1);
                log.setCoinId(1);

            }else if(i <= 10){
                log.setWalletId(1);
                log.setCoinId(2);
            }else{
                log.setWalletId(1);
                log.setCoinId(3);
            }
            TradeType type = (i % 3 == 0) ? TradeType.SELL : TradeType.BUY;

            log.setType(i % 3 == 0 ? TradeType.SELL : TradeType.BUY);

            log.setCreatedAt(baseDate.plusDays((i - 1) * 7));
            logs.add(log);

            log.setQuantity(BigDecimal.valueOf(1));

            BigDecimal price = BigDecimal.valueOf(1000 + (i * 1000));
            log.setPrice(price);
            
        }

        tradeLogService.saveAll(logs);
    }
//    @Test
//    @DisplayName("거래 내역 생성")
//    void t1() throws Exception {
//        ResultActions resultActions = mock
//                .perform(post("/api/tradeLog/wallet/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                    "walletId": 1,
//                                    "siteId": 1,
//                                    "coinId": 1,
//                                    "type": "BUY",
//                                    "quantity": 2.5,
//                                    "price": 30000.0
//                                }
//                                """.stripIndent())
//                        )
//                        .andDo(print());
//        TradeLog tradeLog = tradeLogService.findLatest().get();
//        resultActions
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.data.id").exists())
//                .andExpect(jsonPath("$.data.walletId").value(1))
//                .andExpect(jsonPath("$.data.siteId").value(1))
//                .andExpect(jsonPath("$.data.coinId").value(1))
//                .andExpect(jsonPath("$.data.type").value("BUY"))
//                .andExpect(jsonPath("$.data.quantity").value(2.5))
//                .andExpect(jsonPath("$.data.price").value(30000.0))
//                .andExpect(jsonPath("$.data.createdAt").exists())
//                .andExpect(jsonPath("$.data.updatedAt").exists());
//    }

//    @Test
//    @DisplayName("거래 내역 생성 - 잘못된 apiKey, 유효한 accessToken")
//    void t2() throws Exception {
//
//        ResultActions resultActions = mock
//                .perform(post("/api/tradeLog/wallet/1")
//                        .header("Authorization", "Bearer wrong-api-key " + actorAccessToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                            {
//                                "walletId": 1,
//                                "siteId": 1,
//                                "coinId": 1,
//                                "type": "BUY",
//                                "quantity": 2.5,
//                                "price": 30000.0
//                            }
//                            """.stripIndent())
//                )
//                .andDo(print());
//
//        resultActions
//                .andExpect(handler().handlerType(TradeLogController.class))
//                .andExpect(handler().methodName("write"))
//                .andExpect(status().isCreated());
//    }
//    @Test
//    @DisplayName("거래 내역 생성 - 잘못된 apiKey 쿠키, 유효한 accessToken 쿠키")
//    void t3() throws Exception {
//        User user = userService.findByUsername("user1").get();
//        String actorAccessToken = userService.genAccessToken(user);
//
//        ResultActions resultActions = mock
//                .perform(post("/api/tradeLog/wallet/1")
//                        .cookie(
//                                new Cookie("apiKey", "wrong-api-key"),
//                                new Cookie("accessToken", actorAccessToken)
//                        )                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                            {
//                                "walletId": 1,
//                                "siteId": 1,
//                                "coinId": 1,
//                                "type": "BUY",
//                                "quantity": 2.5,
//                                "price": 30000.0
//                            }
//                            """.stripIndent())
//                )
//                .andDo(print());
//
//        resultActions
//                .andExpect(handler().handlerType(TradeLogController.class))
//                .andExpect(handler().methodName("write"))
//                .andExpect(status().isCreated());
//    }
    @Test
    @DisplayName("거래 내역 전체 조회")
    void t4() throws Exception {
        ResultActions resultActions = mock
                .perform(get("/api/tradeLog/wallet/1")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TradeLogController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(15));
    }
    @Test
    @DisplayName("거래 내역 필터 조회 - 당일, 모든 거래")
    void t5() throws Exception {
        ResultActions resultActions = mock
                .perform(get("/api/tradeLog/wallet/1")
                        .param("startDate", "2025-07-25")
                        .param("endDate", "2025-07-25")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TradeLogController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(1));

    }

    @Test
    @DisplayName("거래 내역 조회 - 일별, 매수 거래")
    void t6() throws Exception {
        ResultActions resultActions = mock
                .perform(get("/api/tradeLog/wallet/1")
                        .param("startDate", "2025-07-27")
                        .param("endDate", "2025-08-27")
                        .param("type", "BUY")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TradeLogController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(3));

    }

    @Test
    @DisplayName("거래 내역 조회 - 월별, 매도 거래")
    void t7() throws Exception {
        ResultActions resultActions = mock
                .perform(get("/api/tradeLog/wallet/1")
                        .param("startDate", "2025-07-01")
                        .param("endDate", "2025-08-31")
                        .param("type", "SELL")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TradeLogController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("시작일이 종료일보다 이후일 때")
    void t8() throws Exception {
        ResultActions resultActions = mock
                .perform(get("/api/tradeLog/wallet/1")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-01-01")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TradeLogController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("모든 필터 없음 (파라미터 없음)")
    void t9() throws Exception {
        ResultActions resultActions = mock
            .perform(get("/api/tradeLog/wallet/1")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print());
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TradeLogController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(15));
    }

    @Test
    @DisplayName("거래 없음")
    void t10() throws Exception {
        ResultActions resultActions = mock
            .perform(get("/api/tradeLog/wallet/1")
                    .param("startDate", "1999-01-01")
                    .param("endDate", "1999-01-31")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TradeLogController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(0));
    }

}
