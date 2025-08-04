package com.back.back9.domain.exchange.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;
    private final LocalDateTime time;
    private final String symbol;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final double volume;

    @JsonCreator
    public ExchangeDTO(
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("candle_date_time_kst") LocalDateTime time,
            @JsonProperty("market") String symbol,
            @JsonProperty("opening_price") BigDecimal open,
            @JsonProperty("high_price") BigDecimal high,
            @JsonProperty("low_price") BigDecimal low,
            @JsonProperty("trade_price") BigDecimal close,
            @JsonProperty("candle_acc_trade_volume") double volume
    ) {
        this.timestamp = timestamp;
        this.time = time;
        this.symbol = symbol;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
}