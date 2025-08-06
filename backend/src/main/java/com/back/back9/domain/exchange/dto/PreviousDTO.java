package com.back.back9.domain.exchange.dto;

import com.back.back9.domain.websocket.vo.CandleInterval;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PreviousDTO {
    private CandleInterval interval;
    private String market;
    private int page;
    private LocalDateTime time;
}
