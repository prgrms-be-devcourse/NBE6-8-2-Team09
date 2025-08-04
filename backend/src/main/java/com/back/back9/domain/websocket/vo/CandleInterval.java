package com.back.back9.domain.websocket.vo;

import lombok.Getter;

public enum CandleInterval {
    SEC("seconds", 1000),
    MIN_1("minutes/1", 1000),
    MIN_30("minutes/30", 1000),
    HOUR_1("minutes/60", 1000),
    DAY("days", 500),
    WEEK("weeks", 150),
    MONTH("months", 50),
    YEAR("years", 10);

    private final String redisKeySuffix;
    @Getter
    private final int maxSize;

    CandleInterval(String redisKeySuffix, int maxSize) {
        this.redisKeySuffix = redisKeySuffix;
        this.maxSize = maxSize;
    }

    public String redisKey(String symbol) {
        return symbol + ":" + redisKeySuffix;
    }

    public String getSuffix() {
        return redisKeySuffix;
    }

    @Override
    public String toString() {
        return redisKeySuffix;
    }

    public static CandleInterval fromWebSocketType(String websocketType) {
        return switch (websocketType) {
            case "candle.1s" -> SEC;
            case "candle.1m" -> MIN_1;
            case "candle.30m" -> MIN_30;
            case "candle.1h" -> HOUR_1;
            case "candle.1d" -> DAY;
            case "candle.1w" -> WEEK;
            case "candle.1M" -> MONTH;
            case "candle.1y" -> YEAR;
            default -> throw new IllegalArgumentException("Unknown WebSocket candle type: " + websocketType);
        };
    }
}