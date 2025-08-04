package com.back.back9.domain.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CoinPriceResponse {
    private String symbol;
    @JsonProperty("trade_price")
    private BigDecimal price;
    private LocalDateTime time;
}