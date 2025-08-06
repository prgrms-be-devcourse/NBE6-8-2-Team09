/*
package com.back.back9.domain.exchange.service;

import com.back.back9.domain.exchange.dto.CoinPriceResponse;
import com.back.back9.domain.exchange.dto.ExchangeDTO;
import com.back.back9.domain.websocket.mock.MockCoinListProvider;
import com.back.back9.domain.websocket.vo.CandleInterval;
import com.back.back9.global.redis.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

//테스트의 반환형식이 일치하면 성공
@Slf4j
class ExchangeServiceTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private ExchangeService exchangeService;

    @Mock
    private MockCoinListProvider coinListProvider;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String SYMBOL = "KRW-DOGE";
    private List<JsonNode> oneSecondCandles;
    private JsonNode latestCandle;

    @BeforeEach
    void setUp() {
        openMocks(this);
        mockCandleData();

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        exchangeService = new ExchangeService(redisService, objectMapper, coinListProvider);

        // Stub mock returns
        when(redisService.getLatestCandle(CandleInterval.SEC, SYMBOL)).thenReturn(oneSecondCandles);
        when(redisService.getPreviousCandlesByRange(eq(CandleInterval.SEC), eq(SYMBOL), anyInt(), any(LocalDateTime.class))).thenReturn(oneSecondCandles);
        when(redisService.getLatest1sCandle(SYMBOL)).thenReturn(latestCandle);
    }

    private void mockCandleData() {
        oneSecondCandles = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        LocalDateTime baseTimestamp = LocalDateTime.of(2025, 8, 4, 11, 19, 32);
        LocalDateTime baseKST = LocalDateTime.of(2025, 8, 4, 11, 9, 50);

        for (int i = 0; i < 200; i++) {
            LocalDateTime kst = baseKST.minusSeconds(i);

            ObjectNode node = objectMapper.createObjectNode();
            node.put("market", SYMBOL);
            node.put("candle_date_time_kst", kst.format(formatter));  // ✅
            node.put("opening_price", 280.0);
            node.put("high_price", 280.0);
            node.put("low_price", 280.0);
            node.put("trade_price", 280.0);
            node.put("timestamp", baseTimestamp.format(formatter));  // ✅
            node.put("candle_acc_trade_price", 58951.2);
            node.put("candle_acc_trade_volume", 210.54);

            oneSecondCandles.add(node);
        }

        // Latest Candle
        LocalDateTime kst = LocalDateTime.of(2025, 8, 4, 11, 19, 32);
        ObjectNode latest = objectMapper.createObjectNode();
        latest.put("SYMBOL", SYMBOL);
        latest.put("candle_date_time_kst", kst.format(formatter));  // ✅
        latest.put("opening_price", 281.0);
        latest.put("high_price", 281.0);
        latest.put("low_price", 281.0);
        latest.put("trade_price", 281.0);
        latest.put("timestamp", kst.format(formatter));  // ✅
        latest.put("candle_acc_trade_price", 1627417.2913538);
        latest.put("candle_acc_trade_volume", 5791.5206098);

        latestCandle = latest;
    }

    @Test
    void getInitialCandleTest() {
        List<ExchangeDTO> result = exchangeService.getInitialCandles(CandleInterval.SEC, SYMBOL);

        assertThat(result).hasSize(200);
        ExchangeDTO dto = result.getFirst();
        assertThat(dto).hasFieldOrProperty("timestamp");
        assertThat(dto).hasFieldOrProperty("open");
        assertThat(dto).hasFieldOrProperty("high");
        assertThat(dto).hasFieldOrProperty("low");
        assertThat(dto).hasFieldOrProperty("close");
        assertThat(dto).hasFieldOrProperty("volume");

        log.info("getInitialCandles 결과: {}", result);
    }

    @Test
    void getPreviousCandlesTest() {
        List<ExchangeDTO> result = exchangeService.getPreviousCandles(CandleInterval.SEC, SYMBOL, 0, LocalDateTime.now() );

        assertThat(result).hasSize(200);
        log.info("getPreviousCandles 결과: {}", result);
    }

    @Test
    void getLatestCandleByScanTest() {
        try (var mocked = mockStatic(CoinSymbolMapper.class)) {
            mocked.when(() -> CoinSymbolMapper.toMarketCode("도지코인")).thenReturn(SYMBOL);

            CoinPriceResponse result = exchangeService.getLatestCandleByScan("도지코인");

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(SYMBOL);
            assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(281.0));
            assertThat(result.getTime()).isBeforeOrEqualTo(LocalDateTime.now());

            log.info("getLatestCandleByScan 결과: {}", result);
        }
    }
}
*/