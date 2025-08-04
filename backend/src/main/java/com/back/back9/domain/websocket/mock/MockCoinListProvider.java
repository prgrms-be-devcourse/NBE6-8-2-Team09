package com.back.back9.domain.websocket.mock;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MockCoinListProvider {

    private static final Map<String, String> SYMBOL_TO_NAME = new LinkedHashMap<>();
    private static final Map<String, String> NAME_TO_SYMBOL = new LinkedHashMap<>();

    static {
        SYMBOL_TO_NAME.put("KRW-XRP", "엑스알피(리플)");
        SYMBOL_TO_NAME.put("KRW-ETH", "이더리움");
        SYMBOL_TO_NAME.put("KRW-BTC", "비트코인");
        SYMBOL_TO_NAME.put("KRW-ENA", "에테나");
        SYMBOL_TO_NAME.put("KRW-TOKAMAK", "토카막네트워크");
        SYMBOL_TO_NAME.put("KRW-SOL", "솔라나");
        SYMBOL_TO_NAME.put("KRW-DOGE", "도지코인");
        SYMBOL_TO_NAME.put("KRW-PENGU", "펭지펭권");
        SYMBOL_TO_NAME.put("KRW-STRIKE", "스트라이크");
        SYMBOL_TO_NAME.put("KRW-USDT", "테더");
        SYMBOL_TO_NAME.put("KRW-XLM", "스텔라루멘");
        SYMBOL_TO_NAME.put("KRW-OM", "만토라");
        SYMBOL_TO_NAME.put("KRW-CKB", "너보스");
        SYMBOL_TO_NAME.put("KRW-SUI", "수이");
        SYMBOL_TO_NAME.put("KRW-SAHARA", "사하라에이아이");
        SYMBOL_TO_NAME.put("KRW-ADA", "에이다");
        SYMBOL_TO_NAME.put("KRW-SOPH", "소픈");
        SYMBOL_TO_NAME.put("KRW-VIRTUAL", "버추얼프로토콜");
        SYMBOL_TO_NAME.put("KRW-HBAR", "헤데라");
        SYMBOL_TO_NAME.put("KRW-ONDO", "온도파이낸스");
        SYMBOL_TO_NAME.put("KRW-TAIKO", "타이코");
        SYMBOL_TO_NAME.put("KRW-ENS", "이더리움네임서비스");
        SYMBOL_TO_NAME.put("KRW-ETC", "이더리움클래식");
        SYMBOL_TO_NAME.put("KRW-ALT", "알트레이어");
        SYMBOL_TO_NAME.put("KRW-SYRUP", "메이플파이낸스");
        SYMBOL_TO_NAME.put("KRW-HYPER", "하이퍼레인");
        SYMBOL_TO_NAME.put("KRW-OMNI", "옴니네트워크");
        SYMBOL_TO_NAME.put("KRW-SEI", "세이");
        SYMBOL_TO_NAME.put("KRW-ATH", "에이서");
        SYMBOL_TO_NAME.put("KRW-WAVES", "웨이브");
        SYMBOL_TO_NAME.put("KRW-BLAST", "블라스트");

        for (Map.Entry<String, String> entry : SYMBOL_TO_NAME.entrySet()) {
            NAME_TO_SYMBOL.put(entry.getValue(), entry.getKey());
        }
    }

    public List<String> getMarketCodes() {
        return new ArrayList<>(SYMBOL_TO_NAME.keySet());
    }

    public Map<String, String> getSymbolToNameMap() {
        return SYMBOL_TO_NAME;
    }

    public Optional<String> getNameBySymbol(String symbol) {
        return Optional.ofNullable(SYMBOL_TO_NAME.get(symbol));
    }

    public Optional<String> getSymbolByName(String name) {
        return Optional.ofNullable(NAME_TO_SYMBOL.get(name));
    }

    public Set<String> getAllCoinNames() {
        return NAME_TO_SYMBOL.keySet();
    }
}