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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;

@Tag("trade_log")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class AnalyticsControllerTest {
    @Autowired
    private AnalyticsController analyticsController;

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("유저 수익률 계산 API - 성공")
    @Test
    void t1() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(get("/api/analytics/wallet/1")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AnalyticsController.class))
                .andExpect(handler().methodName("getUserProfitRate"));
    }
}
