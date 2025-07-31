package com.back.back9.domain.coin.dto;

import com.back.back9.domain.coin.entity.Coin;

import java.time.LocalDateTime;

public record CoinDto(
        long id,
        String symbol,
        String koreanName,
        String englishName,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public CoinDto(Coin coin){
        this(
                coin.getId(),
                coin.getSymbol(),
                coin.getKoreanName(),
                coin.getEnglishName(),
                coin.getCreatedAt(),
                coin.getModifiedAt()
        );
    }
}
