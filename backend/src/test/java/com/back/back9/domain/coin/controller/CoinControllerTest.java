package com.back.back9.domain.coin.controller;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.domain.coin.service.CoinService;
import com.back.back9.global.error.ErrorException;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@JsonPOJOBuilder
@Tag("coin")
public class CoinControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private CoinService coinService;

    @Autowired
    private CoinRepository coinRepository;

    private Coin coin1;
    private Coin coin2;
    private Coin coin3;
    private Coin coin4;

    @BeforeEach
    void setUp() {
        coinRepository.deleteAll();

        coin1 = coinRepository.save(new Coin("BTC","비트코안","Bitcoin"));
        coin2 = coinRepository.save(new Coin("ETH", "이더리움", "Ethereum"));
        coin3 = coinRepository.save(new Coin("XRP","리플","Ripple"));
        coin4 = coinRepository.save(new Coin("DOGE","도지코인","Dogecoin"));
    }


    @Test
    @DisplayName("Coin 전체 조회")
    void getCoins() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/coins")
                )
                .andDo(print());

        List<Coin> testCoin = coinService.findAll();

        resultActions
                .andExpect(handler().handlerType(CoinController.class))
                .andExpect(handler().methodName("getCoins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        for (int i = 0; i < testCoin.size(); i++) {
            Coin coin = testCoin.get(i);
            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(coin.getId()))
                    .andExpect(jsonPath("$[%d].symbol".formatted(i)).value(coin.getSymbol()))
                    .andExpect(jsonPath("$[%d].koreanName".formatted(i)).value(coin.getKoreanName()))
                    .andExpect(jsonPath("$[%d].englishName".formatted(i)).value(coin.getEnglishName()));
        }

    }

    @Test
    @DisplayName("Coin 단건 조회")
    void getCoin() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/coins/" + coin1.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CoinController.class))
                .andExpect(handler().methodName("getCoin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(coin1.getSymbol()));
    }

    @Test
    @DisplayName("Coin 단건 조회, 404")
    void getCoin2() throws Exception {
        int id = 999;

        ResultActions resultActions = mvc
                .perform(
                        get("/coins/" + id)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CoinController.class))
                .andExpect(handler().methodName("getCoin"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    @DisplayName("Coin 추가")
    void addCoin() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/coins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                        "symbol" : "BTC new",
                                        "koreanName" : "비트코인 new",
                                        "englishName" : "Bitcoint new"
                                        }
                                        """)
                ).andDo(print());

        Coin coin = coinService.findLastest().get();


        resultActions
                .andExpect(handler().handlerType(CoinController.class))
                .andExpect(handler().methodName("addCoin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(coin.getId()))
                .andExpect(jsonPath("$.symbol").value(coin.getSymbol()))
                .andExpect(jsonPath("$.koreanName").value(coin.getKoreanName()))
                .andExpect(jsonPath("$.englishName").value(coin.getEnglishName()));
    }

    @Test
    @DisplayName("코인 추가, Without symbol")
    void addCoin2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/coins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                        "symbol" : "",
                                        "koreanName" : "비트코인 new",
                                        "englishName" : "Bitcoint new"
                                        }
                                        """)
                ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CoinController.class))
                .andExpect(handler().methodName("addCoin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"));
    }

    @Test
    @DisplayName("Coin 삭제")
    void deleteCoin() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/coins/" + coin1.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CoinController.class))
                .andExpect(handler().methodName("deleteCoin"))
                .andExpect(status().isOk());

        assertThrows(ErrorException.class, () -> {
            coinService.findById(coin1.getId());
        });
    }

    @Test
    @DisplayName("Coin 수정")
    void modifyCoin() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/coins/" + coin1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                        "symbol" : "BTC 수정",
                                        "koreanName" : "비트코인 수정",
                                        "englishName" : "Bitcoint 수정"
                                        }
                                        """)
                ).andDo(print());

        Coin coin = coinService.findById(coin1.getId());

        resultActions
                .andExpect(handler().handlerType(CoinController.class))
                .andExpect(handler().methodName("modifyCoin"))
                .andExpect(status().isOk());
    }
}