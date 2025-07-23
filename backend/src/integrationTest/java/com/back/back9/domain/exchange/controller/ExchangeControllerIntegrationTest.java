package com.back.back9.domain.exchange.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ExchangeControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void tickerEndpoint_returns200() throws Exception {
        mvc.perform(get("/back9/exchange/ticker"))
                .andExpect(status().isOk());
    }
}
