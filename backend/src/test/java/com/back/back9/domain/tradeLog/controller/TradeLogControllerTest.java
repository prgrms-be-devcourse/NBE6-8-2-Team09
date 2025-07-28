package com.back.back9.domain.tradeLog.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("trade_log")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class TradeLogControllerTest {
    @Autowired
    private TradeLogController TradeLogController;

    @Autowired
    private MockMvc mock;

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
                .andExpect(handler().methodName("getItems"));
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
                .andExpect(handler().methodName("getItems"));
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
                .andExpect(handler().methodName("getItems"));
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
                .andExpect(handler().methodName("getItems"));
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
                .andExpect(handler().methodName("getItems"));
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
                .andExpect(handler().methodName("getItems"));
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
                .andExpect(handler().methodName("getItems"));
    }

}
