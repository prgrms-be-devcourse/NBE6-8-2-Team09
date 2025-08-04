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
}