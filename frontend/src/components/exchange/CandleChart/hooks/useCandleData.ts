import { useCallback, useEffect, useRef, useState } from "react";
import type { CandleInterval, ExchangeDTO } from "@/lib/types/exchange/type";
import { exchangeApi } from "@/lib/api/exchange";

export const useCandleData = (market: string, interval: CandleInterval) => {
    const [displayedCandles, setDisplayedCandles] = useState<ExchangeDTO[]>([]);
    const pageRef = useRef(0);
    const initialCandlesRef = useRef<ExchangeDTO[]>([]);
    const unrenderedCandlesRef = useRef<ExchangeDTO[]>([]);

    const loadInitial = useCallback(async () => {
        try {
            const data = await exchangeApi.getInitialCandles({ market, interval });
            const reversed = data.slice().reverse();
            initialCandlesRef.current = reversed;
            pageRef.current = 1;

            const latest120 = reversed.slice(-50);
            const unrendered = reversed.slice(0, reversed.length - 50);

            setDisplayedCandles(latest120);
            unrenderedCandlesRef.current = unrendered;
        } catch (e) {
            console.error("loadInitial failed:", e);
        }
    }, [market, interval]);

    const loadPrevious = useCallback(async () => {
        try {
            const oldest = displayedCandles[0];
            const fetched = await exchangeApi.getPreviousCandles({
                market,
                interval,
                page: pageRef.current,
                time: oldest.candle_date_time_kst,
            });
            if (!fetched.length) return;

            pageRef.current += 1;
            const reversed = fetched.slice().reverse();
            setDisplayedCandles((prev) => [...reversed, ...prev]);
        } catch (e) {
            console.error("loadPrevious failed:", e);
        }
    }, [displayedCandles, market, interval]);

    const resetCandles = useCallback(() => {
        setDisplayedCandles([]);
        pageRef.current = 0;
        initialCandlesRef.current = [];
        unrenderedCandlesRef.current = [];
    }, []);

    const changeInterval = useCallback((newInterval: CandleInterval) => {
        resetCandles();
    }, [resetCandles]);

    useEffect(() => {
        loadInitial();
    }, [loadInitial]);

    return {
        candles: displayedCandles,
        loadPrevious,
        resetCandles,
        changeInterval,
    };
};