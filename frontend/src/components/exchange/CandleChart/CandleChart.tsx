"use client";

import React, {useCallback, useState} from "react";
import {ChartCanvas} from "./ChartCanvas";
import {ChartHeader} from "./ChartHeader";
import {useCandleData} from "@/components/exchange/CandleChart/hooks/useCandleData";
import {CandleInterval} from "@/lib/types/exchange/type";

export const CandleChart: React.FC<{ market: string; intval: CandleInterval }> = ({
                                                                                      market,
                                                                                      intval,
                                                                                  }) => {
    const [interval, setInterval] = useState<CandleInterval>(intval);
    const [maType, setMAType] = useState<number[]>([15, 50]);

    const { candles, loadPrevious, resetCandles, changeInterval } = useCandleData(
        market,
        interval
    );

    const handleIntervalChange = useCallback(
        (newInterval: CandleInterval) => {
            setInterval(newInterval);
            resetCandles();
            changeInterval(newInterval);
        },
        [resetCandles, changeInterval]
    );

    return (
        <div className="w-full border bg-white flex flex-col">
            {/* 상단 고정 헤더 */}
            <div className="h-[50px] flex-none">
                <ChartHeader
                    market={market}
                    interval={interval}
                    onChange={handleIntervalChange}
                    maType={maType}
                    setMAType={setMAType}
                    onReset={resetCandles}
                />
            </div>

            {/* 차트 캔버스 + 오버레이 (반응형 높이 적용) */}
            <div className="w-full h-[400px] lg:h-[600px] xl:h-[800px] p-2">
                <ChartCanvas candles={candles} interval={interval} symbol={market} />
            </div>
        </div>
    );
};