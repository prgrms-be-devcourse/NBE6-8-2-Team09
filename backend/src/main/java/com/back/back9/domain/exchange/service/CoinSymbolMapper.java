package com.back.back9.domain.exchange.service;

import java.util.HashMap;
import java.util.Map;

public class CoinSymbolMapper {
    private static final Map<String, String> KOREAN_TO_MARKET_MAP = new HashMap<>();

    static {
        KOREAN_TO_MARKET_MAP.put("이더리움", "KRW-ETH");
        KOREAN_TO_MARKET_MAP.put("아베", "KRW-AAVE");
        KOREAN_TO_MARKET_MAP.put("비트코인", "KRW-BTC");
        KOREAN_TO_MARKET_MAP.put("리플", "KRW-XRP");
        KOREAN_TO_MARKET_MAP.put("도지코인", "KRW-DOGE");
        KOREAN_TO_MARKET_MAP.put("트론", "KRW-TRX");
        KOREAN_TO_MARKET_MAP.put("에이다", "KRW-ADA");
        KOREAN_TO_MARKET_MAP.put("스택스", "KRW-STX");
        KOREAN_TO_MARKET_MAP.put("체인링크", "KRW-LINK");
        KOREAN_TO_MARKET_MAP.put("아발란체", "KRW-AVAX");
        KOREAN_TO_MARKET_MAP.put("샌드박스", "KRW-SAND");
        KOREAN_TO_MARKET_MAP.put("솔라나", "KRW-SOL");
    }

    public static String toMarketCode(String koreanName) {
        return KOREAN_TO_MARKET_MAP.getOrDefault(koreanName, null);
    }
}