package com.back.back9.domain.websocket.service;

import com.back.back9.domain.websocket.vo.CandleInterval;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Component
public class FallbackRegistry {

    private final Set<CandleInterval> fallbackSet = new HashSet<>();

    public void enableFallback(CandleInterval interval) {
        fallbackSet.add(interval);
    }

    public boolean isFallback(CandleInterval interval) {
        return fallbackSet.contains(interval);
    }
}