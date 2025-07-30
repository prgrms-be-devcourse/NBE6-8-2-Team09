package com.back.back9.domain.log.tradeLog.service;

import com.back.back9.domain.log.tradeLog.entity.TradeLog;
import com.back.back9.domain.log.tradeLog.entity.TradeType;
import com.back.back9.domain.log.tradeLog.service.TradeLogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Tag("trade_log")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class TradeLogServiceTest {
    @Autowired
    private TradeLogService tradeLogService;

    @Autowired
    private MockMvc mock;

    @Test
    @DisplayName("거래 내역 생성")
    public void createTradeLog() {
        TradeLog tradeLog = new TradeLog();
        tradeLog.setWalletId(22);
        tradeLog.setCoinId(22);
        tradeLog.setType(TradeType.BUY);
        tradeLog.setQuantity(new BigDecimal("0.5"));
        tradeLog.setPrice(new BigDecimal("43000"));
        // when
        TradeLog saved = tradeLogService.save(tradeLog);

        // then
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(22, saved.getWalletId());
    }

}
