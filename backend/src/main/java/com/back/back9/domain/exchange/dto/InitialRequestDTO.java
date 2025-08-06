package com.back.back9.domain.exchange.dto;

import com.back.back9.domain.websocket.vo.CandleInterval;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitialRequestDTO {
    private CandleInterval interval;
    private String market;
}
