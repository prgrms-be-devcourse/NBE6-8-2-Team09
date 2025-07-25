package com.back.back9.domain.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoinPriceResponse {
    private String symbol;
    private String time;
    private String price;
}