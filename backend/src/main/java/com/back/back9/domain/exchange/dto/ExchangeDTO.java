package com.back.back9.domain.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExchangeDTO {
    private String symbol;
    private String time;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private String timestamp;
}