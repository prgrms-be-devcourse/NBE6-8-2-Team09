package com.back.back9.domain.coin.dto;

import jakarta.validation.constraints.NotNull;

public record CoinAddRequest (
        @NotNull(message = "symbol 입력은 필수입니다.")
        String symbol,
        String koreanName,
        String englishName
){}
