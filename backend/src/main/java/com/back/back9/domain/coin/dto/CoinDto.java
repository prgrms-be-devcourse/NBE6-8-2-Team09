package com.back.back9.domain.coin.dto;

import com.back.back9.domain.coin.entity.Coin;

public record CoinDto(
        int id,
        String symbol,
        String koreanName,
        String englishName
) {
    public CoinDto(Coin coin){
        this(
                coin.getId(),
                coin.getSymbol(),
                coin.getKoreanName(),
                coin.getEnglishName()
        );
    }
}
